package com.svega.vanitygen

import java.math.BigInteger
import java.util.zip.CRC32

object GenMnemonic {
    fun getMnemonic(seed_: String): String{
        var seed = seed_
        val words = ArrayList<String>()
        val n = english.size
        for(j in 0 until seed.length step 8){
            seed = seed.substring(0, j) + swapEndian4Byte(seed.substring(j, j+8)) + seed.substring(j + 8)
        }
        for(i in 0 until seed.length step 8){
            val x = BigInteger(seed.substring(i, i + 8), 16).toLong()
            val w1 = (x % n).toInt()
            val w2 = (Math.floor(x.toDouble() / n) + w1).toInt() % n
            val w3 = (Math.floor(Math.floor(x.toDouble() / n) / n) + w2).toInt() % n
            words.add(english[w1])
            words.add(english[w2])
            words.add(english[w3])
        }
        val sb = StringBuilder()
        words.add(words[getChecksumIndex(words, englishPrefix)])
        for(word in words){
            sb.append(word)
            sb.append(" ")
        }
        println(sb.toString())
        return sb.toString()
    }

    private fun swapEndian4Byte(str: String) = str.substring(6, 8) + str.substring(4, 6) + str.substring(2, 4) + str.substring(0, 2)


    private fun getChecksumIndex(words: ArrayList<String>, prefix_len: Int): Int {
        var trimmed_words = "";
        for (i in 0 until words.size) {
            trimmed_words += words[i].substring(0, prefix_len)
        }
        val x = CRC32()
        x.update(trimmed_words.toByteArray())
        var checksum = x.value
        var index = checksum % words.size
        return index.toInt()
    }

}