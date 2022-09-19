package dev.peopo.skuerrel.data

data class SQLPair<T: Any?>(val column: String, val value: T?)