package dev.peopo.apexsql.data

data class SQLProfile(
	val host: String,
	val port: Int,
	val database: String,
	val user: String,
	val password: String,
	val poolSize: Int = 10
)