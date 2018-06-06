package com.svega.vanitygen

import com.svega.vanitygen.VanityGenMain.createHalfAddressString
import com.svega.vanitygen.fxmls.LaunchPage
import com.svega.vanitygen.ge.ge_p3
import com.svega.vanitygen.ge.ge_p3_tobytes
import com.svega.vanitygen.ge.ge_scalarmult_base
import javafx.util.Callback
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

object VanityGenMain{
    fun generateKeys(seed: Array<UInt8>): KeyPair{
        if (seed.size != 32)
            throw MoneroException("Invalid input length!")
        val sec = sc_reduce32(seed)
        val point = ge_p3()
        ge_scalarmult_base.ge_scalarmult_base(point, sec.asByteArray())
        val public = ByteArray(sec.size)
        ge_p3_tobytes.ge_p3_tobytes(public, point)
        val pub = public.asUInt8Array()

        return KeyPair(BinHexUtils.binaryToHex(pub), BinHexUtils.binaryToHex(sec))
    }

    fun load_3(in_: Array<UInt8>, off: Int): Long {
        var result = (in_[0 + off] and 0xff.toUInt8()).toLong()
        result = result or ((in_[1 + off] and 0xff.toUInt8()).toLong() shl 8)
        result = result or ((in_[2 + off] and 0xff.toUInt8()).toLong() shl 16)
        return result
    }

    fun load_4(in_: Array<UInt8>, off: Int): Long{
        var result = (in_[0 + off] and 0xff.toUInt8()).toLong()
        result = result or ((in_[1 + off] and 0xff.toUInt8()).toLong() shl 8)
        result = result or ((in_[2 + off] and 0xff.toUInt8()).toLong() shl 16)
        result = result or ((in_[3 + off] and 0xff.toUInt8()).toLong() shl 24)
        return result
    }

    fun sc_reduce32(s: Array<UInt8>):Array<UInt8> {
        val ret = Arrays.copyOf(s, 32)
        var s0 = 2097151L and load_3(ret, 0)
        var s1 = 2097151L and (load_4(ret, 2) shr 5)
        var s2 = 2097151L and (load_3(ret, 5) shr 2)
        var s3 = 2097151L and (load_4(ret, 7) shr 7)
        var s4 = 2097151L and (load_4(ret, 10) shr 4)
        var s5 = 2097151L and (load_3(ret, 13) shr 1)
        var s6 = 2097151L and (load_4(ret, 15) shr 6)
        var s7 = 2097151L and (load_3(ret, 18) shr 3)
        var s8 = 2097151L and load_3(ret, 21)
        var s9 = 2097151L and (load_4(ret, 23) shr 5)
        var s10 = 2097151L and (load_3(ret, 26) shr 2)
        var s11 = (load_4(ret, 28) shr 7)
        var s12 = 0L
        var carry0: Long
        var carry1: Long
        var carry2: Long
        var carry3: Long
        var carry4: Long
        var carry5: Long
        var carry6: Long
        var carry7: Long
        var carry8: Long
        var carry9: Long
        var carry10: Long
        var carry11: Long

        carry0 = (s0 + (1 shl 20)) shr 21
		s1 += carry0
		s0 -= carry0  shl  21
        carry2 = (s2 + (1 shl 20)) shr 21
		s3 += carry2
		s2 -= carry2  shl  21
        carry4 = (s4 + (1 shl 20)) shr 21
		s5 += carry4
		s4 -= carry4  shl  21
        carry6 = (s6 + (1 shl 20)) shr 21
		s7 += carry6
		s6 -= carry6  shl  21
        carry8 = (s8 + (1 shl 20)) shr 21
		s9 += carry8
		s8 -= carry8  shl  21
        carry10 = (s10 + (1 shl 20)) shr 21
		s11 += carry10
		s10 -= carry10  shl  21

        carry1 = (s1 + (1 shl 20)) shr 21
		s2 += carry1
		s1 -= carry1  shl  21
        carry3 = (s3 + (1 shl 20)) shr 21
		s4 += carry3
		s3 -= carry3  shl  21
        carry5 = (s5 + (1 shl 20)) shr 21
		s6 += carry5
		s5 -= carry5  shl  21
        carry7 = (s7 + (1 shl 20)) shr 21
		s8 += carry7
		s7 -= carry7  shl  21
        carry9 = (s9 + (1 shl 20)) shr 21
		s10 += carry9
		s9 -= carry9  shl  21
        carry11 = (s11 + (1 shl 20)) shr 21
		s12 += carry11
		s11 -= carry11  shl  21

        s0 += s12 * 666643
        s1 += s12 * 470296
        s2 += s12 * 654183
        s3 -= s12 * 997805
        s4 += s12 * 136657
        s5 -= s12 * 683901
        s12 = 0

        carry0 = s0 shr 21
		s1 += carry0
		s0 -= carry0  shl  21
        carry1 = s1 shr 21
		s2 += carry1
		s1 -= carry1  shl  21
        carry2 = s2 shr 21
		s3 += carry2
		s2 -= carry2  shl  21
        carry3 = s3 shr 21
		s4 += carry3
		s3 -= carry3  shl  21
        carry4 = s4 shr 21
		s5 += carry4
		s4 -= carry4  shl  21
        carry5 = s5 shr 21
		s6 += carry5
		s5 -= carry5  shl  21
        carry6 = s6 shr 21
		s7 += carry6
		s6 -= carry6  shl  21
        carry7 = s7 shr 21
		s8 += carry7
		s7 -= carry7  shl  21
        carry8 = s8 shr 21
		s9 += carry8
		s8 -= carry8  shl  21
        carry9 = s9 shr 21
		s10 += carry9
		s9 -= carry9  shl  21
        carry10 = s10 shr 21
		s11 += carry10
		s10 -= carry10  shl  21
        carry11 = s11 shr 21
		s12 += carry11
		s11 -= carry11  shl  21

        s0 += s12 * 666643
        s1 += s12 * 470296
        s2 += s12 * 654183
        s3 -= s12 * 997805
        s4 += s12 * 136657
        s5 -= s12 * 683901

        carry0 = s0 shr 21
		s1 += carry0
		s0 -= carry0  shl  21
        carry1 = s1 shr 21
		s2 += carry1
		s1 -= carry1  shl  21
        carry2 = s2 shr 21
		s3 += carry2
		s2 -= carry2  shl  21
        carry3 = s3 shr 21
		s4 += carry3
		s3 -= carry3  shl  21
        carry4 = s4 shr 21
		s5 += carry4
		s4 -= carry4  shl  21
        carry5 = s5 shr 21
		s6 += carry5
		s5 -= carry5  shl  21
        carry6 = s6 shr 21
		s7 += carry6
		s6 -= carry6  shl  21
        carry7 = s7 shr 21
		s8 += carry7
		s7 -= carry7  shl  21
        carry8 = s8 shr 21
		s9 += carry8
		s8 -= carry8  shl  21
        carry9 = s9 shr 21
		s10 += carry9
		s9 -= carry9  shl  21
        carry10 = s10 shr 21
		s11 += carry10
		s10 -= carry10  shl  21

        ret[0] = (s0 shr 0).toUInt8()
        ret[1] = (s0 shr 8).toUInt8()
        ret[2] = ((s0 shr 16) or (s1  shl  5)).toUInt8()
        ret[3] = (s1 shr 3).toUInt8()
        ret[4] = (s1 shr 11).toUInt8()
        ret[5] = ((s1 shr 19) or (s2  shl  2)).toUInt8()
        ret[6] = (s2 shr 6).toUInt8()
        ret[7] = ((s2 shr 14) or (s3  shl  7)).toUInt8()
        ret[8] = (s3 shr 1).toUInt8()
        ret[9] = (s3 shr 9).toUInt8()
        ret[10] = ((s3 shr 17) or (s4  shl  4)).toUInt8()
        ret[11] = (s4 shr 4).toUInt8()
        ret[12] = (s4 shr 12).toUInt8()
        ret[13] = ((s4 shr 20) or (s5  shl  1)).toUInt8()
        ret[14] = (s5 shr 7).toUInt8()
        ret[15] = ((s5 shr 15) or (s6  shl  6)).toUInt8()
        ret[16] = (s6 shr 2).toUInt8()
        ret[17] = (s6 shr 10).toUInt8()
        ret[18] = ((s6 shr 18) or (s7  shl  3)).toUInt8()
        ret[19] = (s7 shr 5).toUInt8()
        ret[20] = (s7 shr 13).toUInt8()
        ret[21] = (s8 shr 0).toUInt8()
        ret[22] = (s8 shr 8).toUInt8()
        ret[23] = ((s8 shr 16) or (s9  shl  5)).toUInt8()
        ret[24] = (s9 shr 3).toUInt8()
        ret[25] = (s9 shr 11).toUInt8()
        ret[26] = ((s9 shr 19) or (s10  shl  2)).toUInt8()
        ret[27] = (s10 shr 6).toUInt8()
        ret[28] = ((s10 shr 14) or (s11  shl  7)).toUInt8()
        ret[29] = (s11 shr 1).toUInt8()
        ret[30] = (s11 shr 9).toUInt8()
        ret[31] = (s11 shr 17).toUInt8()

        return ret
    }

    fun createHalfAddressString(seed: Array<UInt8>): String{
        val spend = generateKeys(seed)
        return Base58.encode("12${spend.public}")
    }

    fun createFullAddress(seed: Array<UInt8>): String{
        val spend = generateKeys(seed)
        var second = Keccak.getHash(BinHexUtils.hexToByteArray(spend.secret), Parameter.KECCAK_256);
        val view = this.generateKeys(second.asUInt8Array())
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
    }
    fun increaseGenThreads(){
        val run = Thread(Runnable {
            try{
                val sr = SecureRandom()
                val seed = sr.generateSeed(32)
                while(!done){
                    q.add(Pair(createHalfAddressString(seed.asUInt8Array()), Arrays.copyOf(seed, seed.size)))
                    System.arraycopy(seed, 1, seed, 0, 31)
                    seed[31] = sr.nextInt().toByte()
                }
            }catch (_: InterruptedException){}
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
                    println("Generated $generated so far, took ${(etime.toDouble() - stime.toDouble()) / 1e9} seconds" +
                            " (${10000 / ((etime.toDouble() - stime.toDouble()) / 1e9)} addresses per second)")
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
                println("We made $generated addresses in ${(end - start) / 1e9} seconds (${generated / ((end.toDouble() - start.toDouble()) / 1e9)} addresses per second)")
                val seed = BinHexUtils.binaryToHex(pair.second)
                println("Seed for match is $seed")
                val address = VanityGenMain.createFullAddress(pair.second.asUInt8Array())
                println("Address is $address")
                onDoneCallback.call(Pair(address, seed))
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
            generatorThreads[generatorThreads.size - 1].interrupt()
            generatorThreads.removeAt(generatorThreads.size - 1)
        }
        lp.update(LaunchPage.UpdateItem.GEN_THREADS, generatorThreads.size)
    }
    fun stop(){
        done = true
    }

    fun formatDuration(duration: Duration): String {
        val seconds = duration.seconds
        val absSeconds = Math.abs(seconds)
        val positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                absSeconds % 3600 / 60,
                absSeconds % 60)
        return if (seconds < 0) "-$positive" else positive
    }
}

fun main(args: Array<String>){
    /*val s = BinHexUtils.hexToBinary("4141414141414141414141414141414141414141414141414141414141414141")
    val ret = VanityGenMain.sc_reduce32(s)
    println(BinHexUtils.binaryToHex(ret))
    assert(VanityGenMain.generateKeys(s).public == "5CBF23874E021D1D0F44315852D38D4EFE30BA83B6F1E607B3081663F46AB625")
    println("---")
    println(VanityGenMain.createFullAddress(s))
    println("---")
    println(createHalfAddressString(s))*/

    if(args.isEmpty()){
        println("must have at least one arg (regex form) to match!")
        println("this will match from seconds character, disregarding the 4 in front.")
        System.exit(-1)
    }


    val pattern = "^4${args[0]}.*"
    println(pattern)
    VanityGenMain.genVanityAddress(Regex(pattern))
}