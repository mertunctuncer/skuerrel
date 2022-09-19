package dev.peopo.skuerrel.reflection

import dev.peopo.skuerrel.annotation.Column
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

internal inline fun <reified T> createEmptyList(kClass: KClass<out Any>): MutableList<T?> {
	var highestIndex = -1
	for (property in kClass.memberProperties) {
		val column = property.findAnnotation<Column>() ?: continue
		if (highestIndex < column.index) highestIndex = column.index
	}
	if (highestIndex == -1) throw IllegalArgumentException("No property with @Column annotation found")
	return MutableList(highestIndex + 1) { null }
}