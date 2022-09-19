package dev.peopo.apexsql.impl.query.row

import dev.peopo.apexsql.Table
import dev.peopo.apexsql.data.SQLPairList
import dev.peopo.apexsql.impl.query.Query
import java.sql.Connection
import java.sql.SQLException

internal class InsertQuery(connection: Connection, table: Table, private val set: SQLPairList) : Query(connection, table) {

	override val query: String = "INSERT INTO ${table.name}(${set.asKeySyntax()}) VALUES (${set.asValueSyntax()});"

	fun execute() = try {
		prepareStatement()
		for (i in set.withIndex()) statement!!.setObject(i.index + 1, i.value)
		executeUpdate()
		commit()
	} catch (e: SQLException) {
		handleException(e)
	} finally {
		close()
	}

}