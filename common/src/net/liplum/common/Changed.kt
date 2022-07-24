package net.liplum.common

import arc.util.Time

data class Changed<T>(
    val last: T?,
    val timestamp: Float = Time.time,
) {
    companion object {
        fun <T> empty() = Changed<T>(null, 0f)
    }
}