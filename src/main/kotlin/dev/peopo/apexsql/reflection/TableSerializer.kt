package dev.peopo.apexsql.reflection

import dev.peopo.apexsql.annotation.*
import dev.peopo.apexsql.data.ColumnData
import java.sql.Timestamp
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.withNullability

object TableSerializer {
	internal fun serialize(kClass: KClass<out Any>): List<ColumnData> {
		val columnArray = createEmptyList<ColumnData>(kClass)

		if (kClass.primaryConstructor != null) throw IllegalArgumentException("Primary constructors are not supported.")

		for (property in kClass.memberProperties) {
			val column = property.findAnnotation<Column>() ?: continue
			val name = property.findAnnotation<Name>()?.name ?: property.name
			val type = property.findAnnotation<DataType>()?.type ?: serializeToType(property)
			val size = if (column.size == -1) null else column.size
			val columnData = if (property is KMutableProperty<*>) {
				ColumnData(name, type, size)
			} else throw IllegalArgumentException("Field must not be final")

			columnData.notNull = !property.returnType.isMarkedNullable
			columnData.primaryKey = property.findAnnotation<PrimaryKey>() != null
			columnData.foreignKey = property.findAnnotation<ForeignKey>() != null
			columnData.default = property.findAnnotation<Default>()?.defaultText
			columnData.unique = property.findAnnotation<Unique>() != null

			columnArray[column.index] = columnData
		}

		return columnArray.map { it!! }
	}

	private fun serializeToType(property: KProperty1<out Any, *>) = when (property.returnType.withNullability(false).classifier) {
		String::class -> "VARCHAR"
		Char::class -> "CHAR"
		Float::class -> "FLOAT"
		Long::class -> "BIGINT"
		Int::class -> "INT"
		Boolean::class -> "BOOLEAN"
		Double::class -> "DOUBLE PRECISION"
		Timestamp::class -> "TIMESTAMP"
		else -> throw IllegalArgumentException("Unsupported data type")
	}
}