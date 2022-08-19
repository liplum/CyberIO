package net.liplum.common

import java.lang.annotation.Inherited

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
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class UseReflection