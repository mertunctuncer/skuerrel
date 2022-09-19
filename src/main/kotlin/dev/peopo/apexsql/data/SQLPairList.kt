package dev.peopo.apexsql.data

class SQLPairList{
	private val pairs = arrayListOf<SQLPair<Any?>>()

	val size
		get() = pairs.size
	val lastIndex
		get() = pairs.lastIndex

	fun add(column: String, value: Any?) = pairs.add(SQLPair(column, value))
	fun add(pair : SQLPair<Any?>) = pairs.add(pair)

	fun getColumn(index: Int) = pairs[index].column
	fun getValue(index: Int) = pairs[index].value

	fun withIndex() = pairs.withIndex()
	operator fun iterator() = pairs.iterator()

	internal fun asKeySyntax() : String{
		var keyStr = ""
		for(pair in pairs) keyStr += "${pair.column}, "
		return keyStr.dropLast(2)
	}

	internal fun asValueSyntax() : String{
		var valueStr = ""
		for(pair in pairs) valueStr += "?, "
		return valueStr.dropLast(2)
	}

	internal fun asSetSyntax() : String {
		var setStr = ""
		for(pair in pairs) setStr += "${pair.column} = ?, "
		return setStr.dropLast(2)
	}

	internal fun asWhereSyntax() : String {
		var whereStr = ""
		for(pair in pairs) whereStr += "${pair.column} = ? AND "
		return whereStr.dropLast(5)
	}
}