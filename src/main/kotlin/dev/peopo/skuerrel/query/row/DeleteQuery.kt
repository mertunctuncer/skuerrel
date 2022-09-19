package dev.peopo.skuerrel.query.row

import dev.peopo.skuerrel.Table
import dev.peopo.skuerrel.data.SQLPairList
import dev.peopo.skuerrel.query.Query
import java.sql.Connection
import java.sql.SQLException

class DeleteQuery(connection: Connection, table: Table<*>, private val where: SQLPairList? = null) : Query(connection, table) {

	override val query: String

	init {
		var deleteQuery = "DELETE FROM ${table.name}"
		if(where != null) deleteQuery += " WHERE ${where.asWhereSyntax()}"
		query = "$deleteQuery;"
	}

	fun execute() = try {
		prepareStatement()
		if(where != null) for (i in where.withIndex()) statement!!.setObject(i.index + 1, i.value)
		executeUpdate()
		commit()
	} catch (e: SQLException) {
		handleException(e)
	} finally {
		close()
	}
}
