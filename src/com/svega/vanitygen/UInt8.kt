package com.svega.vanitygen

import java.math.BigInteger

data class UInt8(private var value: Int) : Number(), Comparable<UInt8>{
    init{
        value = value and 0xFF
    }
    override fun toByte() = value.toByte()
    override fun toChar() = value.toChar()
    override fun toInt() = value
    override fun toLong() = value.toLong()
    override fun toDouble() = value.toDouble()
    override fun toFloat() = value.toFloat()
    override fun toShort() = value.toShort()
    fun toBigInteger() = BigInteger(value.toString())

    operator fun inc() = UInt8(value + 1)
    operator fun dec() = UInt8(value - 1)

    override fun compareTo(other: UInt8) = value.compareTo(other.value)
    override fun toString() = toInt().toString()
    fun set(new: Int){
        print("Setting as $new which turns into ")
        value = new and 0xF
        println("$value")
    }
    fun set(new: Char) = set(new.toInt())
    fun get() = value
    override fun equals(other: Any?): Boolean {
        if(other == null)
            return false
        if(other !is UInt8)
            return false
        return value == other.value
    }
}

infix fun UInt8.or(other: UInt8) = toInt().or(other.toInt()).toUInt8()
infix fun UInt8.and(other: UInt8) = toInt().and(other.toInt()).toUInt8()

fun Byte.toUInt8() = UInt8(this.toInt())
fun Char.toUInt8() = UInt8(this.toInt())
fun Int.toUInt8() = UInt8(this)
fun Long.toUInt8() = UInt8(this.toInt())
fun Double.toUInt8() = UInt8(this.toInt())
fun Float.toUInt8() = UInt8(this.toInt())
fun Short.toUInt8() = UInt8(this.toInt())

fun Array<UInt8>.asString() : String{
    val sb = StringBuilder()
    for(b in this){
        sb.append(b.toChar())
    }
    return sb.toString()
}

fun Array<UInt8>.asByteArray(): ByteArray {
    return ByteArray(this.size, {x -> this[x].toByte()})
}

fun ByteArray.asUInt8Array() = Array(this.size, {x -> this[x].toUInt8()})