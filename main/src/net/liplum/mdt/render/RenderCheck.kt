package net.liplum.mdt.render

import arc.Core
import arc.math.geom.Geometry
import arc.math.geom.Rect
import arc.math.geom.Vec2
import net.liplum.lib.math.Point2f

private val r1 = Rect()
private val r2 = Rect()
fun inViewField(x: Float, y: Float, clip: Float): Boolean {
    return Core.camera.bounds(r1).overlaps(r2.setCentered(x, y, clip))
}

fun Point2f.inViewField(clip: Float): Boolean {
    return Core.camera.bounds(r1).overlaps(r2.setCentered(x, y, clip))
}
/**
 * Get the raycast between two points in view field
 * @param start the start point
 * @param end the ebd point
 * @return the raycast vector or null if the raycast line can't be seen
 */
fun raycastInViewField(start: Point2f, end: Point2f): Vec2? {
    return Geometry.raycastRect(start.x, start.y, end.x, end.y, Core.camera.bounds(r1))
}
/**
 * Get whether the line between two point can be seen in the view field
 * @param start the start point
 * @param end the ebd point
 * @return if it can be seen, return true
 */
fun isLineInViewField(start: Point2f, end: Point2f): Boolean {
    return raycastInViewField(start, end) != null
}