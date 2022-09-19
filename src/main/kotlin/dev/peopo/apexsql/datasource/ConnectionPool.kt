package dev.peopo.apexsql.datasource

import java.sql.Connection

interface ConnectionPool {

	fun getConnection(): Connection
	fun close()
}