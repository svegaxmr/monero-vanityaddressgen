package com.svega.vanitygen

class BinHexUtils {
    companion object {
        fun hexToBinary(hex: String) = hexToByteArray(hex).asUInt8Array()

        fun hexToByteArray(s: String): ByteArray {
            val len = s.length

            if (len % 2 != 0)
                throw MoneroException("Hex string has invalid length!")
            if(s.isEmpty())
                return byteArrayOf()

            val out = ByteArray(len / 2)

            var i = 0
            while (i < len) {
                val h = hexToBin(s[i])
                val l = hexToBin(s[i + 1])
                if (h == -1 || l == -1) {
                    throw IllegalArgumentException(
                            "contains illegal character for hexBinary: $s")
                }

                out[i / 2] = (h * 16 + l).toByte()
                i += 2
            }

            return out
        }

        private fun hexToBin(ch: Char): Int {
            if (ch in '0'..'9') {
                return ch - '0'
            }
            if (ch in 'A'..'F') {
                return ch - 'A' + 10
            }
            return if (ch in 'a'..'f') {
                ch - 'a' + 10
            } else -1
        }

        fun convertHexToString(hex: String) = String(hexToByteArray(hex))

        private val hexCode = "0123456789ABCDEF".toCharArray()
        private fun printHexBinary(data: ByteArray): String{
            val r = StringBuilder(data.size * 2)
            for (b in data) {
                r.append(hexCode[b.toInt() shr 4 and 0xF])
                r.append(hexCode[b.toInt() and 0xF])
            }
            return r.toString()
        }

        fun binaryToHex(bin: Array<UInt8>) = printHexBinary(bin.asByteArray())

        fun binaryToHex(bin: ByteArray)  = printHexBinary(bin)

        fun binaryToHex(bin: List<Byte>) = printHexBinary(bin.toByteArray())

        fun stringToBinary(str: String) : Array<UInt8>{
            val bytes = str.toByteArray()
            val ret = Array(bytes.size, {_ -> UInt8(0)})
            for(i in 0 until bytes.size){
                ret[i] = bytes[i].toUInt8()
            }
            return ret
        }

        fun binaryToString(bin: Array<UInt8>) : String {
            val cr = CharArray(bin.size)

            for(i in 0 until bin.size){
                cr[i] = bin[i].toChar()
                if(cr[i].toInt() >= 128)
                    cr[i] = (cr[i].toInt() and 0x00FF).toChar()
            }

            return String(cr)
        }
    }
}