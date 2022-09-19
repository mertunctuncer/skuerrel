package dev.peopo.apexsql.impl.query.table

import dev.peopo.apexsql.SQLTable
import dev.peopo.apexsql.impl.query.SQLQuery
import java.sql.SQLException

internal class DropTableQuery(table: SQLTable) : SQLQuery(table) {
	override val query: String

	init {
		query = "DROP TABLE ${table.name};"
	}

	fun execute() = try {
		prepareStatement()
		executeUpdate()
		commit()
	} catch (e: SQLException) {
		handleException(e)
	} finally {
		close()
	}
}