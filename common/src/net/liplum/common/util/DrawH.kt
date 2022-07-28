@file:JvmName("DrawH")

package net.liplum.common.util

import arc.graphics.g2d.Draw

/**
 * Draw anything on a new z-index, and it will turn back after drawing.
 */
inline fun DrawLayer(layer: Float = Draw.z(), draw: () -> Unit) {
    val original = Draw.z()
    Draw.z(layer)
    draw()
    Draw.z(original)
}
/**
 * Draw anything on new pixel scale, and it will turn back after drawing.
 */
inline fun DrawScale(draw: () -> Unit) {
    val originalXscl = Draw.xscl
    val originalYscl = Draw.yscl
    val originalScl = Draw.scl
    draw()
    Draw.xscl = originalXscl
    Draw.yscl = originalYscl
    Draw.scl = originalScl
}