package dev.peopo.apexsql.impl.data

internal class ColumnData<T : Any>(
	val name: String,
	val dataType: String,
	val size: Int? = null,
	val default: T? = null,
	val primaryKey: Boolean = false,
	val foreignKey: Boolean = false,
	val unique: Boolean = false,
	val notNull: Boolean = false
)