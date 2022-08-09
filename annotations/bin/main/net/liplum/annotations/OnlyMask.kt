package net.liplum.annotations

/**
 * **[pos] Starts with 0**
 * @return whether the [pos] of bits is 1.
 */
infix fun Int.isOn(pos: Int): Boolean = (this shr pos and 1) != 0

object Only {
    // @formatter:off
    const val client     = 1 shl 0
    const val debug      = 1 shl 1
    const val headless   = 1 shl 2
    const val steam      = 1 shl 3
    const val unsteam    = 1 shl 4
    const val desktop    = 1 shl 5
    const val mobile     = 1 shl 6
    // @formatter:on
}
@JvmInline
value class OnlySpec(val only: Int) {
    // @formatter:off
    val client: Boolean     get() = only isOn 0
    val debug: Boolean      get() = only isOn 1
    val headless: Boolean   get() = only isOn 2
    val steam: Boolean      get() = only isOn 3
    val unsteam: Boolean    get() = only isOn 4
    val desktop: Boolean    get() = only isOn 5
    val mobile: Boolean     get() = only isOn 6
    // @formatter:on
    companion object {
        fun Int.toOnlySpec() = OnlySpec(this)
    }
}