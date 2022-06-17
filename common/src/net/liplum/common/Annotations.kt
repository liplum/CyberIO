package net.liplum.common

import java.lang.annotation.Inherited

/**
 * It indicates this property/field should be serialized into save or datapack.
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Serialized
/**
 * It indicates this function use random number which may not be synchronized on Physical Server between Physical Client
 * so that you have to send data packet manually to share data.
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class UseRandom
/**
 * It indicates reflection is used there. Please pay attention to the API changes between versions.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.EXPRESSION,
    AnnotationTarget.LOCAL_VARIABLE,
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class UseReflection
/**
 * It indicates a function is idempotent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Idempotent
/**
 * It indicates this parameter is used to output something.
 * ## Use case:
 * 1. For mutable object: fun reflect(@Out out:Vec2)
 * This function should set any field of `out` vector
 * 2. When it's used on extension function, the receiver is the output
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class Out