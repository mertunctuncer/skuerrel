package dev.peopo.skuerrel.query.table

import dev.peopo.skuerrel.Table
import dev.peopo.skuerrel.query.Query
import java.sql.Connection
import java.sql.SQLException


class CreateTableQuery(connection: Connection, table: Table<*>, ifNotExist: Boolean = true) : Query(connection, table) {

	override val query: String

	init {
		var create = ""
		for (column in table.columns) {
			val size = if (column.size != null) "(${column.size})" else ""
			val primaryKey = if (column.primaryKey) " PRIMARY KEY" else ""
			val foreignKey = if (column.foreignKey) " FOREIGN KEY" else ""
			val default = if (column.default != null) " DEFAULT ${column.default}" else ""
			val unique = if (column.unique) " UNIQUE" else ""
			val notNull = if (column.notNull) " NOT NULL" else ""
			create += "${column.name} ${column.dataType}$size$primaryKey$foreignKey$unique$notNull$default, "
		}
		create = create.dropLast(2)
		val exist = if (ifNotExist) "IF NOT EXISTS " else ""
		query = "CREATE TABLE $exist${table.name}($create);"
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
