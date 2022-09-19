package dev.peopo.apexsql.impl.query.row

import dev.peopo.apexsql.SQLTable
import dev.peopo.apexsql.data.SQLPairList
import dev.peopo.apexsql.impl.query.SQLQuery
import java.sql.SQLException

internal class SelectQuery(table: SQLTable) : SQLQuery(table) {
	private var pairList : SQLPairList? = null
	private var whereString : String? = null
	private var whereValues : List<Any?>? = null

	constructor(table: SQLTable, where: SQLPairList? = null) : this(table) {
		pairList = where
	}

	constructor(table: SQLTable, whereString: String, whereValues: List<Any?>) : this(table){
		this.whereString = whereString
		this.whereValues = whereValues
	}

	override val query: String

	init {
		var selectQuery = "SELECT * FROM ${table.name}"
		if(whereString != null) selectQuery += " WHERE $whereString"
		else if(pairList != null) selectQuery += " WHERE ${pairList!!.asWhereSyntax()}"
		query = "$selectQuery;"
	}

	fun execute() : List<Map<String, Any?>> {
		val results = mutableListOf<Map<String, Any?>>()

		try {
			prepareStatement()

			if(whereValues != null) for(i in whereValues!!.withIndex()) statement!!.setObject(i.index + 1, i.value)
			else if(pairList != null) for (i in pairList!!.withIndex()) statement!!.setObject(i.index + 1, i.value)

			result = statement!!.executeQuery()

			cacheResults(results)
			commit()
		} catch (e: SQLException) {
			handleException(e)
		} finally {
			close()
		}

		return results
	}

	private fun cacheResults(resultList: MutableList<Map<String, Any?>>) : List<Map<String, Any?>> {
		var index = 0
		while (result!!.next()) {
			index++
			val map: MutableMap<String, Any?> = HashMap()
			for(columnIndex in table.columns.indices) {
				map[table.columns[columnIndex].name] = result!!.getObject(columnIndex + 1)
			}
			resultList.add(map)
		}
		return resultList
	}

}