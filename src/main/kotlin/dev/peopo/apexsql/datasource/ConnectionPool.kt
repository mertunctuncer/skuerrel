package dev.peopo.apexsql.datasource

import java.sql.Connection

interface ConnectionPool {

	fun getConnection(): Connection

	fun canConnect() : Boolean  = try {
		val conn = getConnection()
		conn.close()
		true
	} catch (e: Exception) {
		false
	}

	fun close()
}