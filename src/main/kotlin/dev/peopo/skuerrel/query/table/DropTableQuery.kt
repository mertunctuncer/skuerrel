package dev.peopo.skuerrel.query.table

import dev.peopo.skuerrel.Table
import dev.peopo.skuerrel.query.Query
import java.sql.Connection
import java.sql.SQLException

class DropTableQuery(connection: Connection, table: Table<*>) : Query(connection, table) {
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