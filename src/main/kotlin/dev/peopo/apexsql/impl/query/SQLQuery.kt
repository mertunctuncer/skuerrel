package dev.peopo.apexsql.impl.query

import dev.peopo.apexsql.SQLTable
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

internal abstract class SQLQuery(protected val table: SQLTable) {
	protected var connection : Connection? = null
	protected var statement : PreparedStatement? = null
	protected var result : ResultSet? = null

	abstract val query: String

	init {
		try {
			TODO("HANDLE CONNECTION")
		} catch (e: SQLException) {
			handleException(e)
			close()
		}
	}

	protected fun handleException(e: SQLException) {
		e.printStackTrace()
		connection?.rollback()
	}

	protected fun close() {
		result?.close()
		statement?.close()
		connection?.close()
	}

	fun prepareStatement() {
		statement = connection!!.prepareStatement(query)
	}
	fun executeUpdate() = statement!!.executeUpdate()
	fun commit() = connection!!.commit()

}