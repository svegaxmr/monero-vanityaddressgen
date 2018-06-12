package com.svega.vanitygen

import com.svega.vanitygen.VanityGenMain.createHalfAddressString
import javafx.util.Callback
import java.awt.event.ActionListener
import java.security.SecureRandom
import java.time.Duration
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

object VanityGenMain{
    fun createHalfAddressString(seed: Array<UInt8>): String{
        val spend = CryptoOps.generateKeys(seed)
        return Base58.encode("12${spend.public}")
    }

    fun createFullAddress(seed: Array<UInt8>): String{
        val spend = CryptoOps.generateKeys(seed)
        val second = Keccak.getHash(BinHexUtils.hexToByteArray(spend.secret), Parameter.KECCAK_256)
        val view = CryptoOps.generateKeys(second.asUInt8Array())
        val toHash = "12${spend.public}${view.public}"
        val checksum = Keccak.checksum(BinHexUtils.hexToBinary(toHash))
        return Base58.encode(toHash + BinHexUtils.binaryToHex(checksum))
    }

    fun cliVanityAddress(regex: String, scanner: Scanner?){
        val complexity = MoneroVanityGenMain.getComplexity(regex)
        val q = LinkedBlockingQueue<Pair<String, ByteArray>>()
        val cb = Callback<Triple<String, String, String>, Unit>{
            println("Address is ${it.first}")
            println("Seed is ${it.second}")
            println("Mnemonic is ${it.third}")
            println("If this helped you, please consider donation to ${Utils.DONATION_ADDRESS}")
        }
        val clh = CLIHandler(scanner)
        clh.update(UpdateItem.COMPLEXITY, complexity)
        val vgs = VanityGenState(clh, q, cb, Regex("^4$regex.*"))
        while(vgs.isWorking()){
            Thread.sleep(250)
        }
    }
    fun startAsGUI(lp: ProgressUpdatable, reg: String, cb: Callback<Triple<String, String, String>, Unit>): VanityGenState{
        val q = LinkedBlockingQueue<Pair<String, ByteArray>>()
        val pattern = "^4$reg.*"
        return VanityGenState(lp, q, cb, Regex(pattern))
    }
}

class VanityGenState(private val lp: ProgressUpdatable,
                     private val q: LinkedBlockingQueue<Pair<String, ByteArray>>,
                     private val onDoneCallback: Callback<Triple<String, String, String>, Unit>,
                     private val regex: Regex) {
    private var done = false
    private val start = System.nanoTime()
    private val validationThreads = ArrayList<Thread>()
    private val generatorThreads = ArrayList<Thread>()
    private var genNumber = 0
    private var valNumber = 0
    private var generated = 0
    private var stime = start
    private val timer: javax.swing.Timer
    private var lastGen = 0
    init {
        increaseGenThreads()
        increaseValidationThreads()
        lp.update(UpdateItem.SELF, this)
        lp.update(UpdateItem.STATUS, "Working...")
        lp.update(UpdateItem.POST_GEN, "")
        timer = javax.swing.Timer(1000, {
            val etime = System.nanoTime()
            val tm = Duration.ofNanos(etime - start)
            lp.update(UpdateItem.TIME, tm.seconds)
            lp.update(UpdateItem.ADDRESSES_PER_SEC, ((generated - lastGen) / ((etime.toDouble() - stime.toDouble()) / 1e9)).toLong())
            lp.update(UpdateItem.NUMBER_GEN, generated)
            lp.update(UpdateItem.QDEPTH, q.size)
            stime = etime
            lastGen = generated
        })
        timer.isRepeats = true
        timer.start()
    }
    fun increaseGenThreads(){
        val run = Thread(Runnable {
            val sr = SecureRandom()
            val seed = sr.generateSeed(32)
            while(!done){
                q.add(Pair(createHalfAddressString(seed.asUInt8Array()), Arrays.copyOf(seed, seed.size)))
                System.arraycopy(seed, 1, seed, 0, 31)
                seed[31] = sr.nextInt().toByte()
            }
            Thread.sleep(0)
        })
        run.name = "Generator Thread $genNumber"
        run.start()
        ++genNumber
        generatorThreads.add(run)
        lp.update(UpdateItem.GEN_THREADS, generatorThreads.size)
    }
    fun increaseValidationThreads(){
        val run = Thread(Runnable {
            var pair = q.poll(1000, TimeUnit.MILLISECONDS)
            var match = pair.first
            var result = regex.matches(match)
            ++generated
            while(!result && !done){
                pair = q.poll(1000, TimeUnit.MILLISECONDS)
                match = pair.first
                result = regex.matches(match)
                ++generated
            }
            if(!done) {
                done = true
                val end = System.nanoTime()
                val tm = Duration.ofNanos(end - start)
                lp.update(UpdateItem.TIME, tm.seconds)
                lp.update(UpdateItem.ADDRESSES_PER_SEC, (generated / ((end.toDouble() - start.toDouble()) / 1e9)).toLong())
                lp.update(UpdateItem.NUMBER_GEN, generated)
                lp.update(UpdateItem.STATUS, "Done!")
                val seed = BinHexUtils.binaryToHex(pair.second)
                val address = VanityGenMain.createFullAddress(pair.second.asUInt8Array())
                val mnemonic = GenMnemonic.getMnemonic(seed)
                onDoneCallback.call(Triple(address, seed, mnemonic))
                lp.update(UpdateItem.POST_GEN, "If this helped you, please consider donating!")
                lp.update(UpdateItem.MNEMONIC, mnemonic)
                timer.stop()
            }
        })
        run.name = "Validation Thread $valNumber"
        run.start()
        ++valNumber
        validationThreads.add(run)
        lp.update(UpdateItem.VAL_THREADS, validationThreads.size)
    }
    fun decreaseGenThreads(){
        if(generatorThreads.size != 1) {
            generatorThreads[generatorThreads.size - 1].stop()
            generatorThreads.removeAt(generatorThreads.size - 1)
        }
        lp.update(UpdateItem.GEN_THREADS, generatorThreads.size)
    }
    fun stop(){
        done = true
        timer.stop()
    }
    fun isWorking() = !done
}