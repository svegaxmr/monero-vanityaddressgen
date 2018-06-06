package com.svega.vanitygen

import com.svega.vanitygen.BinHexUtils.Companion.binaryToString
import com.svega.vanitygen.BinHexUtils.Companion.hexToBinary
import com.svega.vanitygen.BinHexUtils.Companion.stringToBinary
import java.math.BigInteger

class Base58 {
    companion object{

        val alphabetStr = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        private val alphabet = alphabetStr.toByteArray()
        private val encodedBlockSizes = intArrayOf(0, 2, 3, 5, 6, 7, 9, 10, 11)
        private val alphabetSize = alphabet.size
        private val fullBlockSize = 8
        private val fullEncodedBlockSize = 11

        private fun uint8BufToUInt64(data: Array<UInt8>) : BigInteger{
            if (data.isEmpty() || data.size > 8) {
                throw MoneroException("Invalid input length "+data.size)
            }
            var res = BigInteger.ZERO
            val twoPow8 = BigInteger("2").pow(8)
            var i = 0
            var c = 9 - data.size
            while (c <= 8) {
                res = when(c == 1) {
                    true -> res.add(BigInteger.valueOf(data[i++].toLong()))
                    false -> res.multiply(twoPow8).add(BigInteger.valueOf(data[i++].toLong()))
                }
                ++c
            }
            return res
        }

        private fun uint64ToUInt8Buf(num_: BigInteger, size: Int) : Array<UInt8> {
            if (size < 1 || size > 8) {
                throw MoneroException("Invalid input length")
            }

            var num = num_
            val res = Array(size, {_ -> UInt8(0)})
            val twoPow8 = BigInteger("2").pow(8)
            for (i in size - 1 downTo 0) {
                res[i] = num.remainder(twoPow8).toLong().toUInt8()
                num = num.divide(twoPow8)
            }
            return res
        }

        private fun encodeBlock(data: Array<UInt8>, buf: Array<UInt8>, index: Int) : Array<UInt8> {
            if (data.isEmpty() || data.size > fullEncodedBlockSize) {
                throw MoneroException("Invalid block length: " + data.size)
            }
            var num = uint8BufToUInt64(data)
            var i = encodedBlockSizes[data.size] - 1
            // while num > 0
            while (num > BigInteger.ZERO) {
                val div = num.divideAndRemainder(alphabetSize.toBigInteger())
                // remainder = num % alphabetSize
                val remainder = div[1]
                // num = num / alphabet_size
                num = div[0]
                buf[index + i] = alphabet[remainder.toInt()].toUInt8()
                i--
            }
            return buf
        }

        @Throws(MoneroException::class)
        fun encode(hex: String) : String{
            val data = hexToBinary(hex)
            if (data.isEmpty()) {
                return ""
            }
            val fullBlockCount = Math.floor(data.size.toDouble() / fullBlockSize).toInt()
            val lastBlockSize = data.size % fullBlockSize
            val resSize = (fullBlockCount * fullEncodedBlockSize + encodedBlockSizes[lastBlockSize])
            var res = Array(resSize, {_ -> UInt8(0)})
            for (i in 0 until resSize) {
                res[i] = alphabet[0].toUInt8()
            }
            for (i in 0 until fullBlockCount) {
                res = encodeBlock(data.sliceArray(IntRange(i * fullBlockSize, i * fullBlockSize + fullBlockSize - 1)), res, i * fullEncodedBlockSize)
            }
            if (lastBlockSize > 0) {
                res = encodeBlock(data.sliceArray(IntRange(fullBlockCount * fullBlockSize, fullBlockCount * fullBlockSize + lastBlockSize - 1)), res, fullBlockCount * fullEncodedBlockSize)
            }
            return binaryToString(res)
        }

        private fun decodeBlock(data: Array<UInt8>, buf: Array<UInt8>, index: Int) : Array<UInt8> {
            if (data.isEmpty() || data.size > fullEncodedBlockSize) {
                throw MoneroException("Invalid block length: " + data.size)
            }
            val resSize = encodedBlockSizes.indexOf(data.size)
            if (resSize <= 0) {
                throw MoneroException("Invalid block size")
            }
            var resNum = BigInteger.ZERO
            var order = BigInteger.ONE
            for (i in data.size - 1 downTo 0) {
                val digit = alphabet.indexOf(data[i].toByte())
                if (digit < 0) {
                    throw MoneroException("Invalid symbol")
                }
                val product = order.multiply(digit.toBigInteger()).add(resNum)
                // if product > UINT64_MAX
                if (product > BigInteger("2").pow(64)) {
                    throw MoneroException("Overflow")
                }
                resNum = product
                order = order.multiply(alphabetSize.toBigInteger())
            }
            if (resSize < fullBlockSize && (BigInteger("2").pow(8 * resSize) < resNum)) {
                throw MoneroException("Overflow 2")
            }
            val bytes = uint64ToUInt8Buf(resNum, resSize)
            System.arraycopy(bytes, 0, buf, index, bytes.size)
            return buf
        }

        @Throws(MoneroException::class)
        fun decode(enc_: String): Array<UInt8> {
            val enc = stringToBinary(enc_)
            if (enc.isEmpty()) {
                return Array(0, {_ -> UInt8(0) })
            }
            val fullBlockCount = Math.floor(enc.size.toDouble() / fullEncodedBlockSize).toInt()
            val lastBlockSize = enc.size % fullEncodedBlockSize
            val lastDecodedBlockSize = encodedBlockSizes.indexOf(lastBlockSize)
            if (lastDecodedBlockSize < 0) {
                throw MoneroException("Invalid encoded length")
            }
            val dataSize = fullBlockCount * fullBlockSize + lastDecodedBlockSize
            var data = Array(dataSize, {_ -> UInt8(0) })
            for (i in 0 until fullBlockCount) {
                data = decodeBlock(enc.sliceArray(IntRange(i * fullEncodedBlockSize, i * fullEncodedBlockSize + fullEncodedBlockSize - 1)), data, i * fullBlockSize)
            }
            if (lastBlockSize > 0) {
                data = decodeBlock(enc.sliceArray(IntRange(fullBlockCount * fullEncodedBlockSize, fullBlockCount * fullEncodedBlockSize + lastBlockSize - 1)), data, fullBlockCount * fullBlockSize)
            }
            return data
        }

        fun encodeAddr(tag: Long, data: String) : String{
            TODO("Not yet implemented")
        }
        fun decodeAddr(addr: String, tag: Long) : ByteArray{
            TODO("Not yet implemented")
        }
    }
}