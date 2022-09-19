package dev.peopo.apexsql

import dev.peopo.apexsql.impl.data.ColumnData


class SQLTable(val database: String, val name: String) {

	internal val columns = arrayListOf<ColumnData<*>>()
}