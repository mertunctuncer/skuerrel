package dev.peopo.apexsql

import dev.peopo.apexsql.impl.data.ColumnData


class Table(val name: String) {

	internal val columns = arrayListOf<ColumnData<*>>()
}