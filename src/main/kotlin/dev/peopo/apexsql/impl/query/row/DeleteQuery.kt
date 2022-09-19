package dev.peopo.apexsql.impl.query.row

import dev.peopo.apexsql.SQLTable
import dev.peopo.apexsql.data.SQLPairList
import dev.peopo.apexsql.impl.query.SQLQuery
import java.sql.SQLException

internal class DeleteQuery(table: SQLTable, private val where: SQLPairList? = null) : SQLQuery(table) {

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
