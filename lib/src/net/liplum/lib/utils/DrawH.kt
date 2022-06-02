@file:JvmName("DrawH")

package net.liplum.lib.utils

import arc.graphics.g2d.Draw

inline fun DrawLayer(draw: () -> Unit) {
    val original = Draw.z()
    draw()
    Draw.z(original)
}