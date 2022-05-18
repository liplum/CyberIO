package net.liplum.lib

import arc.graphics.g2d.TextureRegion
import net.liplum.mdt.DrawSize

class Tx(var tr: TextureRegion) {
    /**
     * The offset of the true center for draw between [texture][tr] center.
     */
    var offsetX = 0
    var offsetY = 0
    /**
     * The offset of the true center for draw between [texture][tr] center.
     */
    var dx = 0f
    var dy = 0f
    /**
     * The offset of the 90-degree between [texture][tr] rotation.
     */
    var dr = 0f
    var scale = 1f
    fun draw(x: Float, y: Float, rotation: Float) {
        tr.DrawSize(getX(x), getY(y), size = scale, rotation = getRotation(rotation))
    }

    fun getX(relative: Float): Float {
        return relative + offsetX + dx
    }

    fun getY(relative: Float): Float {
        return relative + offsetY + dy
    }

    fun getRotation(relative: Float): Float {
        return relative + dr
    }
}
