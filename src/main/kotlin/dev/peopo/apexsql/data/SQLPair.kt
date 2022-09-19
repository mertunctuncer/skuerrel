package dev.peopo.apexsql.data

data class SQLPair<T: Any?>(val column: String, val value: T?)