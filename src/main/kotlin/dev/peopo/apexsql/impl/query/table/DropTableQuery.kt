package dev.peopo.apexsql.impl.query.table

import dev.peopo.apexsql.Table
import dev.peopo.apexsql.impl.query.Query
import java.sql.Connection
import java.sql.SQLException

internal class DropTableQuery(connection: Connection, table: Table) : Query(connection, table) {
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