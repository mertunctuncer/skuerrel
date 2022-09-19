package dev.peopo.apexsql.impl.query

import dev.peopo.apexsql.Table
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

internal abstract class Query(protected val connection: Connection, protected val table: Table) {
	protected var statement : PreparedStatement? = null
	protected var result : ResultSet? = null

	abstract val query: String

	protected fun handleException(e: SQLException) {
		e.printStackTrace()
		connection.rollback()
	}

	protected fun close() {
		result?.close()
		statement?.close()
		connection.close()
	}

	fun prepareStatement() {
		statement = connection.prepareStatement(query)
	}

	fun executeUpdate() = statement!!.executeUpdate()
	fun commit() = connection.commit()

}