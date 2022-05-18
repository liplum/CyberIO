package net.liplum.annotations

/**
 * It will automatically resolve the dependency relationship of annotated functions
 * and resort them into a function call in order.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class DependOn(
    /**
     * Another function that this depends on.
     */
    vararg val dependencies: String,
)
