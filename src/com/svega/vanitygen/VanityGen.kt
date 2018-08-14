package com.svega.vanitygen

import com.svega.crypto.common.algos.Keccak
import com.svega.crypto.common.algos.Parameter
import com.svega.moneroutils.Base58
import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.addresses.MoneroAddress.Companion.generateKeys
import com.svega.vanitygen.VanityGenMain.createHalfAddressString
import javafx.util.Callback
import java.security.SecureRandom
import java.time.Duration
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

object VanityGenMain{
    fun createHalfAddressString(seed: ByteArray): String{
        val spend = generateKeys(seed) //view is generated from first seed round
        return Base58.encode("12${spend.public.str}")
    }

    fun createFullAddress(seed: ByteArray): String{
        val spend = generateKeys(seed) //view is generated from first seed round
        val second = Keccak.getHash(spend.secret!!.data, Parameter.KECCAK_256) //keccak256 of view gets spend
        val view = generateKeys(second) //spend is got from generatekeys
        val toHash = "12${spend.public.str}${view.public.str}" //get everything minus checksum
        val checksum = Keccak.checksum(BinHexUtils.hexToBinary(toHash)) //calc checksum
        return Base58.encode(toHash + BinHexUtils.binaryToHex(checksum)) //encode
    }

    fun cliVanityAddress(regex: String, scanner: Scanner?){
        val complexity = MoneroVanityGenMain.getComplexity(regex)
        val cb = Callback<Triple<String, String, String>, Unit>{
            println("Address is ${it.first}")
            println("Seed is ${it.second}")
            println("Mnemonic is ${it.third}")
            println("If this helped you, please consider donation to ${Utils.DONATION_ADDRESS}")
        }
        val clh = CLIHandler(scanner)
        clh.update(UpdateItem.COMPLEXITY, complexity)
        val vgs = VanityGenState(clh, cb, Regex("^4$regex.*"))
        while(vgs.isWorking()){
            Thread.sleep(250)
        }
    }
    fun startAsGUI(lp: ProgressUpdatable, reg: String, cb: Callback<Triple<String, String, String>, Unit>): VanityGenState{
        val pattern = "^4$reg.*"
        return VanityGenState(lp, cb, Regex(pattern))
    }
}

class VanityGenState(private val lp: ProgressUpdatable,
                     private val onDoneCallback: Callback<Triple<String, String, String>, Unit>,
                     private val regex: Regex) {
    private val start = System.nanoTime()
    private val generatorThreads = ArrayList<Thread>()
    private var genNumber = 0
    private var generated = 0
    private var stime = start
    private val timer: javax.swing.Timer
    private var lastGen = 0
    init {
        increaseGenThreads()
        lp.update(UpdateItem.SELF, this)
        lp.update(UpdateItem.STATUS, "Working...")
        lp.update(UpdateItem.POST_GEN, "")
        timer = javax.swing.Timer(1000) {
            val etime = System.nanoTime()
            val tm = Duration.ofNanos(etime - start)
            lp.update(UpdateItem.TIME, tm.seconds)
            lp.update(UpdateItem.ADDRESSES_PER_SEC, ((generated - lastGen) / ((etime.toDouble() - stime.toDouble()) / 1e9)).toLong())
            lp.update(UpdateItem.NUMBER_GEN, generated)
            stime = etime
            lastGen = generated
        }
        timer.isRepeats = true
        timer.start()
    }
    fun increaseGenThreads(){
        val run = Thread(Runnable {
            val sr = SecureRandom()
            val seed = sr.generateSeed(32)
            while(true){
                if(Thread.interrupted())
                    return@Runnable
                for(i in 0 until 31){
                    val match = createHalfAddressString(seed)
                    ++generated
                    val result = regex.matches(match)
                    if(result) {
                        val end = System.nanoTime()
                        val tm = Duration.ofNanos(end - start)
                        lp.update(UpdateItem.TIME, tm.seconds)
                        lp.update(UpdateItem.ADDRESSES_PER_SEC, (generated / ((end.toDouble() - start.toDouble()) / 1e9)).toLong())
                        lp.update(UpdateItem.NUMBER_GEN, generated)
                        lp.update(UpdateItem.STATUS, "Done!")
                        val seed2 = BinHexUtils.binaryToHex(seed)
                        val address = VanityGenMain.createFullAddress(seed)
                        val mnemonic = GenMnemonic.getMnemonic(seed2)
                        onDoneCallback.call(Triple(address, seed2, mnemonic))
                        lp.update(UpdateItem.POST_GEN, "If this helped you, please consider donating!")
                        lp.update(UpdateItem.MNEMONIC, mnemonic)
                        stop()
                    }
                    val temp = seed[i]
                    seed[i] = seed[i + 1]
                    seed[i + 1] = temp
                }
                seed[31] = sr.nextInt().toByte()
            }
        })
        run.name = "Generator Thread $genNumber"
        run.start()
        ++genNumber
        generatorThreads.add(run)
        lp.update(UpdateItem.GEN_THREADS, generatorThreads.size)
    }
    fun decreaseGenThreads(){
        if(generatorThreads.isNotEmpty()) {
            generatorThreads[generatorThreads.size - 1].interrupt()
            generatorThreads.removeAt(generatorThreads.size - 1)
        }
        lp.update(UpdateItem.GEN_THREADS, generatorThreads.size)
    }
    fun stop(){
        for(i in 0 until generatorThreads.size) {
            generatorThreads[i].interrupt()
            generatorThreads.removeAt(i)
        }
        timer.stop()
    }
    fun isWorking() = generatorThreads.isNotEmpty()
}