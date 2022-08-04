package net.liplum.common.util

import arc.Core
import arc.math.geom.Geometry
import arc.math.geom.Position
import arc.math.geom.Rect
import arc.math.geom.Vec2
import plumy.core.math.Point2f

private val r1 = Rect()
private val r2 = Rect()
fun inViewField(x: Float, y: Float, clip: Float): Boolean {
    return Core.camera.bounds(r1).overlaps(r2.setCentered(x, y, clip))
}

fun Position.inViewField(clip: Float): Boolean {
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
    return Core.camera.bounds(r1).raycastInThis(start, end)
}
/**
 * Get the raycast between two points in the rect given
 * @param start the start point
 * @param end the ebd point
 * @return the raycast vector or null if the raycast line can't be seen
 */
fun Rect.raycastInThis(start: Point2f, end: Point2f): Vec2? {
    return Geometry.raycastRect(start.x, start.y, end.x, end.y, this)
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