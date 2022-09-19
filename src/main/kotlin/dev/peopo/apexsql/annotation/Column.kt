package dev.peopo.apexsql.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Column(
	val index: Int,
	val name: String,
	val size: Int = -1
)
