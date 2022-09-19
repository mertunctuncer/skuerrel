package dev.peopo.apexsql.impl.query.row

import dev.peopo.apexsql.SQLTable
import dev.peopo.apexsql.data.SQLPairList
import dev.peopo.apexsql.impl.query.SQLQuery
import java.sql.SQLException

internal class InsertQuery(table: SQLTable, private val set: SQLPairList) : SQLQuery(table) {

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