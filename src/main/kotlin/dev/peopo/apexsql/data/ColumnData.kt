package dev.peopo.apexsql.data

internal data class ColumnData(
	val name: String,
	val dataType: String,
	val size: Int?,
) {
	var primaryKey: Boolean = false
	var default: String? = null
	var foreignKey: Boolean = false
	var unique: Boolean = false
	var notNull: Boolean = false
}