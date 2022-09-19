package dev.peopo.apexsql.query.row

import dev.peopo.apexsql.Table
import dev.peopo.apexsql.data.SQLPairList
import dev.peopo.apexsql.query.Query
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

class UpdateQuery(connection: Connection, table: Table<*>, private val set: SQLPairList, private val where: SQLPairList?) : Query(connection, table) {

	override val query: String

	init {
		var lockQuery = "SELECT * FROM ${table.name}"
		if(where != null) lockQuery += " WHERE ${where.asWhereSyntax()}"
		lockQuery += " FOR UPDATE;"
		query = lockQuery
	}

	fun execute() = try {
		statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
		if (where != null) {
			for (i in where.withIndex()) {
				this.statement!!.setObject(i.index + 1, i.value)
			}
		}
		result = statement!!.executeQuery()
		while (result!!.next()) {
			for (j in set.withIndex()) result!!.updateObject(set.getColumn(j.index), j.value)
			result!!.updateRow()
		}
		commit()
	} catch (e: SQLException) {
		handleException(e)
	} finally {
		close()
	}
}