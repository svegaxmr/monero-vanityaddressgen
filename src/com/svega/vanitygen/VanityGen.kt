package com.svega.vanitygen

import com.svega.vanitygen.VanityGenMain.createHalfAddressString
import com.svega.vanitygen.fxmls.LaunchPage
import javafx.util.Callback
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
        var second = Keccak.getHash(BinHexUtils.hexToByteArray(spend.secret), Parameter.KECCAK_256);
        val view = CryptoOps.generateKeys(second.asUInt8Array())
        println("spend ${spend.public} view ${view.public}")
        val toHash = "12${spend.public}${view.public}"
        val checksum = Keccak.checksum(BinHexUtils.hexToBinary(toHash))
        return Base58.encode(toHash + BinHexUtils.binaryToHex(checksum))
    }

    fun genVanityAddress(regex: Regex){
        val start = System.nanoTime()

        val q = LinkedBlockingQueue<Pair<String, ByteArray>>()
        var done = false

        val TRACE_AMOUNT = 100000

        var a = 0
        Thread(Runnable {
            var pair = q.poll(1000, TimeUnit.MILLISECONDS)
            var match = pair.first
            var result = regex.matches(match)
            var stime = System.nanoTime()
            while(!result){
                ++a
                if(a % TRACE_AMOUNT == 0){
                    val etime = System.nanoTime()
                    println("Generated $a so far, took ${(etime.toDouble() - stime.toDouble()) / 1e9} seconds" +
                            " (${TRACE_AMOUNT / ((etime.toDouble() - stime.toDouble()) / 1e9)} addresses per second)")
                    stime = etime
                    if(q.size >= 500)
                        println("WARN: Queue depth is ${q.size}. Consider decreasing worker threads, or increasing validation threads.")
                }
                pair = q.poll(1000, TimeUnit.MILLISECONDS)
                match = pair.first
                result = regex.matches(match)
            }
            done = true
            val end = System.nanoTime()
            println("We made $a addresses in ${(end - start) / 1e9} seconds (${a / ((end.toDouble() - start.toDouble()) / 1e9)} addresses per second)")
            val seed = BinHexUtils.binaryToHex(pair.second)
            println("Seed for match is $seed")
            val address = VanityGenMain.createFullAddress(pair.second.asUInt8Array())
            println("Address is $address")
        }).start()

        Thread(Runnable {
            val sr = SecureRandom()
            val seed = sr.generateSeed(32)
            while(!done){
                q.add(Pair(createHalfAddressString(seed.asUInt8Array()), Arrays.copyOf(seed, seed.size)))
                System.arraycopy(seed, 1, seed, 0, 31)
                seed[31] = sr.nextInt().toByte()
            }
        }).start()

        Thread(Runnable {
            val sr = SecureRandom()
            val seed = sr.generateSeed(32)
            while(!done){
                q.add(Pair(createHalfAddressString(seed.asUInt8Array()), Arrays.copyOf(seed, seed.size)))
                System.arraycopy(seed, 1, seed, 0, 31)
                seed[31] = sr.nextInt().toByte()
            }
        }).start()

        val sr = SecureRandom()
        val seed = sr.generateSeed(32)
        while(!done){
            q.add(Pair(createHalfAddressString(seed.asUInt8Array()), Arrays.copyOf(seed, seed.size)))
            System.arraycopy(seed, 1, seed, 0, 31)
            seed[31] = sr.nextInt().toByte()
        }
    }
    fun startAsGUI(lp: LaunchPage, reg: String, cb: Callback<Pair<String, String>, Unit>): VanityGenState{
        val q = LinkedBlockingQueue<Pair<String, ByteArray>>()
        val pattern = "^4$reg.*"
        println(pattern)
        return VanityGenState(lp, q, cb, Regex(pattern))
    }
}

class VanityGenState(private val lp: LaunchPage,
                     private val q: LinkedBlockingQueue<Pair<String, ByteArray>>,
                     private val onDoneCallback: Callback<Pair<String, String>, Unit>,
                     private val regex: Regex) {
    private var done = false
    private val start = System.nanoTime()
    private val validationThreads = ArrayList<Thread>()
    private val generatorThreads = ArrayList<Thread>()
    private var genNumber = 0
    private var valNumber = 0
    private var generated = 0
    init {
        increaseGenThreads()
        increaseValidationThreads()
        lp.update(LaunchPage.UpdateItem.STATUS, "Working...")
        lp.update(LaunchPage.UpdateItem.POST_GEN, "")
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
        lp.update(LaunchPage.UpdateItem.GEN_THREADS, generatorThreads.size)
    }
    fun increaseValidationThreads(){
        val run = Thread(Runnable {
            var pair = q.poll(1000, TimeUnit.MILLISECONDS)
            var match = pair.first
            var result = regex.matches(match)
            var stime = System.nanoTime()
            ++generated
            while(!result && !done){
                pair = q.poll(1000, TimeUnit.MILLISECONDS)
                match = pair.first
                result = regex.matches(match)
                ++generated
                if(generated % 10000 == 0){
                    val etime = System.nanoTime()
                    val tm = Duration.ofNanos(etime - start)
                    lp.update(LaunchPage.UpdateItem.TIME, tm.seconds)
                    lp.update(LaunchPage.UpdateItem.ADDRESSES_PER_SEC, (10000 / ((etime.toDouble() - stime.toDouble()) / 1e9)).toLong())
                    lp.update(LaunchPage.UpdateItem.NUMBER_GEN, generated)
                    lp.update(LaunchPage.UpdateItem.QDEPTH, q.size)
                    stime = etime
                }
            }
            if(!done) {
                done = true
                val end = System.nanoTime()
                val tm = Duration.ofNanos(end - start)
                lp.update(LaunchPage.UpdateItem.TIME, tm.seconds)
                lp.update(LaunchPage.UpdateItem.ADDRESSES_PER_SEC, (generated / ((end.toDouble() - start.toDouble()) / 1e9)).toLong())
                lp.update(LaunchPage.UpdateItem.NUMBER_GEN, generated)
                lp.update(LaunchPage.UpdateItem.STATUS, "Done!")
                val seed = BinHexUtils.binaryToHex(pair.second)
                val address = VanityGenMain.createFullAddress(pair.second.asUInt8Array())
                onDoneCallback.call(Pair(address, seed))
                lp.update(LaunchPage.UpdateItem.POST_GEN, "If this helped you, please consider donating!")
                lp.update(LaunchPage.UpdateItem.MNEMONIC, GenMnemonic.getMnemonic(seed))
            }
        })
        run.name = "Validation Thread $valNumber"
        run.start()
        ++valNumber
        validationThreads.add(run)
        lp.update(LaunchPage.UpdateItem.VAL_THREADS, validationThreads.size)
    }
    fun decreaseGenThreads(){
        if(generatorThreads.size != 1) {
            generatorThreads[generatorThreads.size - 1].stop()
            generatorThreads.removeAt(generatorThreads.size - 1)
        }
        lp.update(LaunchPage.UpdateItem.GEN_THREADS, generatorThreads.size)
    }
    fun stop(){
        done = true
    }
}

fun main(args: Array<String>){
    if(args.isEmpty()){
        println("must have at least one arg (regex form) to match!")
        println("this will match from seconds character, disregarding the 4 in front.")
        System.exit(-1)
    }


    val pattern = "^4${args[0]}.*"
    println(pattern)
    VanityGenMain.genVanityAddress(Regex(pattern))
}