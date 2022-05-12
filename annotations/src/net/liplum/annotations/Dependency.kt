package net.liplum.annotations

@Target(AnnotationTarget.FUNCTION)
annotation class DependOn(
    /**
     * Another function that this depends on.
     */
    vararg val dependencies: String,
)
