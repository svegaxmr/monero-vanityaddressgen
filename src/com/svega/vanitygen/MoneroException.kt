package com.svega.vanitygen

class MoneroException : Exception {
    constructor() : super()
    constructor(err: String) : super(err)
    constructor(e: Exception) : super(e)
    constructor(err: String, e: Exception) : super(err, e)
}