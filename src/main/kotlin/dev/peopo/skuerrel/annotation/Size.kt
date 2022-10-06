package dev.peopo.skuerrel.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Size(
	val size: Int
)
