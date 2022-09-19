package dev.peopo.skuerrel.query

import dev.peopo.skuerrel.Table
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

abstract class Query(protected val connection: Connection, protected val table: Table<*>) {
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

	protected fun prepareStatement() {
		statement = connection.prepareStatement(query)
	}

	protected fun executeUpdate() = statement!!.executeUpdate()
	protected fun commit() = connection.commit()

}