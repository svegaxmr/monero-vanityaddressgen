package com.svega.vanitygen

interface ProgressUpdatable {
    fun update(item: UpdateItem, data: Any)
}


enum class UpdateItem {
    TIME,
    NUMBER_GEN,
    VAL_THREADS,
    GEN_THREADS,
    WARN_TEXT,
    ADDRESS,
    SEED,
    MNEMONIC,
    ADDRESSES_PER_SEC,
    STATUS,
    COMPLEXITY,
    QDEPTH,
    POST_GEN,
    SELF
}