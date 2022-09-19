package dev.peopo.apexsql.data

data class SQLPair<String, T: Any?>(val column: String, val value: T?)