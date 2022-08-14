package net.liplum.common

import arc.math.Mathf
import arc.util.Time

class Smooth(
    var value: Float = 0f,
) {
    var target = { 0f }
    var speed = 1f
    fun update(delta: Float = Time.delta) {
        value = Mathf.approach(value, target(), speed * delta)
    }

    fun target(target: () -> Float): Smooth {
        this.target = target
        return this
    }

    fun speed(speed: Float): Smooth {
        this.speed = speed
        return this
    }
}