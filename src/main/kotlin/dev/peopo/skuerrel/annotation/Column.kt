package dev.peopo.skuerrel.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Column(
	val index: Int
)
