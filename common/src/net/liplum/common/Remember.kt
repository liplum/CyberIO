package net.liplum.common

import arc.util.Time

data class Remember<T>(
    val old: T?,
    val timestamp: Float = Time.time,
) {
    companion object {
        fun <T> empty() = Remember<T>(null, 0f)
    }
}