package com.svega.vanitygen

import java.math.BigInteger
import java.lang.System.arraycopy
import java.io.ByteArrayOutputStream

import java.lang.Math.min
import java.util.Arrays.fill

class Keccak{

    companion object {
        private val BIT_64 = BigInteger("18446744073709551615")

        /**
         * Do hash.
         *
         * @param message input data
         * @param parameter keccak param
         * @return byte-array result
         */
        fun getHash(message: ByteArray, parameter: Parameter): ByteArray {
            val uState = IntArray(200)
            val uMessage = convertToUint(message)


            val rateInBytes = parameter.r / 8
            var blockSize = 0
            var inputOffset = 0

            // Absorbing phase
            while (inputOffset < uMessage.size) {
                blockSize = min(uMessage.size - inputOffset, rateInBytes)
                for (i in 0 until blockSize) {
                    uState[i] = uState[i] xor uMessage[i + inputOffset]
                }

                if (blockSize == rateInBytes) {
                    doKeccakf(uState)
                    blockSize = 0
                }

                inputOffset += blockSize
            }

            // Padding phase
            uState[blockSize] = uState[blockSize] xor parameter.d
            if (parameter.d and 0x80 != 0 && blockSize == rateInBytes - 1) {
                doKeccakf(uState)
            }

            uState[rateInBytes - 1] = uState[rateInBytes - 1] xor 0x80
            doKeccakf(uState)

            // Squeezing phase
            val byteResults = ByteArrayOutputStream()
            var tOutputLen = parameter.outputLengthInBytes
            while (tOutputLen > 0) {
                blockSize = min(tOutputLen, rateInBytes)
                for (i in 0 until blockSize) {
                    byteResults.write(uState[i].toByte().toInt())
                }

                tOutputLen -= blockSize
                if (tOutputLen > 0) {
                    doKeccakf(uState)
                }
            }

            return byteResults.toByteArray()
        }

        private fun doKeccakf(uState: IntArray) {
            val lState = Array<Array<BigInteger>>(5) { Array(5, {BigInteger.ZERO}) }

            for (i in 0 until 5) {
                for (j in 0 until 5) {
                    val data = IntArray(8)
                    arraycopy(uState, 8 * (i + 5 * j), data, 0, data.size)
                    lState[i][j] = convertFromLittleEndianTo64(data)
                }
            }
            roundB(lState)

            fill(uState, 0)
            for (i in 0 until 5) {
                for (j in 0 until 5) {
                    val data = convertFrom64ToLittleEndian(lState[i][j])
                    arraycopy(data, 0, uState, 8 * (i + 5 * j), data.size)
                }
            }

        }

        /**
         * Permutation on the given state.
         *
         * @param state state
         */
        private fun roundB(state: Array<Array<BigInteger>>) {
            var LFSRstate = 1
            for (round in 0 until 24) {
                val C = Array(5, {BigInteger.ZERO})
                val D = Array(5, {BigInteger.ZERO})

                // θ step
                for (i in 0 until 5) {
                    C[i] = state[i][0] xor state[i][1] xor state[i][2] xor state[i][3] xor state[i][4]
                }

                for (i in 0 until 5) {
                    D[i] = C[(i + 4) % 5] xor leftRotate64(C[(i + 1) % 5], 1)
                }

                for (i in 0 until 5) {
                    for (j in 0 until 5) {
                        state[i][j] = state[i][j] xor D[i]
                    }
                }

                //ρ and π steps
                var x = 1
                var y = 0
                var current = state[x][y]
                for (i in 0 until 24) {
                    val tX = x
                    x = y
                    y = (2 * tX + 3 * y) % 5

                    val shiftValue = current
                    current = state[x][y]

                    state[x][y] = leftRotate64(shiftValue, (i + 1) * (i + 2) / 2)
                }

                //χ step
                for (j in 0 until 5) {
                    val t = Array<BigInteger>(5, {BigInteger.ZERO})
                    for (i in 0 until 5) {
                        t[i] = state[i][j]
                    }

                    for (i in 0 until 5) {
                        // ~t[(i + 1) % 5]
                        val invertVal = t[(i + 1) % 5] xor BIT_64
                        // t[i] ^ ((~t[(i + 1) % 5]) & t[(i + 2) % 5])
                        state[i][j] = t[i] xor (invertVal and (t[(i + 2) % 5]))
                    }
                }

                //ι step
                for (i in 0 until 7) {
                    LFSRstate = (LFSRstate shl 1 xor (LFSRstate shr 7) * 0x71) % 256
                    // pow(2, i) - 1
                    val bitPosition = (1 shl i) - 1
                    if (LFSRstate and 2 != 0) {
                        state[0][0] = state[0][0] xor (BigInteger.ONE shl bitPosition)
                    }
                }
            }
        }

        private val ENCODE_BYTE_TABLE = byteArrayOf('0'.toByte(), '1'.toByte(), '2'.toByte(), '3'.toByte(), '4'.toByte(), '5'.toByte(), '6'.toByte(), '7'.toByte(), '8'.toByte(), '9'.toByte(), 'a'.toByte(), 'b'.toByte(), 'c'.toByte(), 'd'.toByte(), 'e'.toByte(), 'f'.toByte())

        /**
         * Convert byte array to unsigned array.
         *
         * @param data byte array
         * @return unsigned array
         */
        fun convertToUint(data: ByteArray): IntArray {
            val converted = IntArray(data.size)
            for (i in data.indices) {
                converted[i] = data[i].toInt() and 0xFF
            }

            return converted
        }

        /**
         * Convert LE to 64-bit value (unsigned long).
         *
         * @param data data
         * @return 64-bit value (unsigned long)
         */
        fun convertFromLittleEndianTo64(data: IntArray): BigInteger {
            var uLong = BigInteger.ZERO
            for (i in 0 until 8) {
                uLong += data[i].toBigInteger() shl (8 * i)
            }

            return uLong
        }

        /**
         * Convert 64-bit (unsigned long) value to LE.
         *
         * @param uLong 64-bit value (unsigned long)
         * @return LE
         */
        fun convertFrom64ToLittleEndian(uLong: BigInteger): IntArray {
            val data = IntArray(8)
            val mod256 = BigInteger("256")
            for (i in 0 until 8) {
                data[i] = ((uLong shr (8 * i)) % mod256).toInt()
            }

            return data
        }

        /**
         * Bitwise rotate left.
         *
         * @param value  unsigned long value
         * @param rotate rotate left
         * @return result
         */
        fun leftRotate64(value: BigInteger, rotate: Int): BigInteger {
            val lp = value shr (64 - rotate % 64)
            val rp = value shl (rotate % 64)

            return lp + rp % BigInteger("18446744073709551616")
        }

        /**
         * Convert bytes to string.
         *
         * @param data bytes array
         * @return string
         */
        fun convertBytesToString(data: ByteArray): String {
            val buffer = ByteArrayOutputStream()
            for (i in data.indices) {
                val uVal = (data[i].toInt() and 0xFF).toByte()

                buffer.write(ENCODE_BYTE_TABLE[uVal.toInt() ushr 4].toInt())
                buffer.write(ENCODE_BYTE_TABLE[(uVal.toInt() and 0xF).toByte().toInt()].toInt())
            }

            return String(buffer.toByteArray())
        }

        fun addressChecksum(address: Array<UInt8>) = checksum(address.sliceArray(IntRange(0, address.size - 5)))

        fun checksum(address: Array<UInt8>) = getHash(address.asByteArray(), Parameter.KECCAK_256).asUInt8Array().sliceArray(IntRange(0, 3))
    }
}

enum class Parameter constructor(val r: Int,
                                 val outputLengthInBytes: Int,
                                 val d: Int) {

    KECCAK_224(1152, 28, 0x01),
    KECCAK_256(1088, 32, 0x01),
    KECCAK_384(832, 48, 0x01),
    KECCAK_512(576, 64, 0x01),
    SHA3_224(1152, 28, 0x06),
    SHA3_256(1088, 32, 0x06),
    SHA3_384(832, 48, 0x06),
    SHA3_512(576, 64, 0x06),
    SHAKE128(1344, 32, 0x1F),
    SHAKE256(1088, 64, 0x1F)
}