package dev.peopo.apexsql.reflection

import dev.peopo.apexsql.annotation.*
import dev.peopo.apexsql.data.SQLPair
import dev.peopo.apexsql.data.SQLPairList
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.*

object DataSerializer {

	inline fun <reified T : Any> deserialize(values: Map<String, Any?>): T {
		val kClass = T::class
		val instance = kClass.createInstance()
		for (property in kClass.memberProperties) {
			if (!property.hasAnnotation<Column>()) continue
			if (property is KMutableProperty<*>) property.setter.call(values[property.name])
			else throw IllegalAccessException("Propery is not mutable")
		}
		return instance
	}

	fun serialize(any: Any) : SQLPairList {
		val kClass = any::class
		val values = createEmptyList<SQLPair<*>>(kClass)
		for(property in kClass.memberProperties) {
			val column = property.findAnnotation<Column>() ?: continue
			values[column.index] = SQLPair(property.name, property.getter.call(any))
		}
		return SQLPairList(values.mapTo(mutableListOf()) { it!! })
	}
}





