package net.liplum.animations

import arc.math.Mathf

open class Floating {
    val minX: Float
    val minY: Float
    val maxX: Float
    val maxY: Float
    var changeRate = 0
    var xOffset = 0f
        set(value) {
            field = value.coerceIn(minX, maxX)
        }
    var yOffset = 0f
        set(value) {
            field = value.coerceIn(minY, maxY)
        }
    @JvmField var xAdding = false
    @JvmField var yAdding = false

    constructor(range: Float) {
        minX = -range
        minY = -range
        maxX = range
        maxY = range
    }

    constructor(
        minX: Float,
        minY: Float,
        maxX: Float,
        maxY: Float,
    ) {
        this.minX = minX
        this.minY = minY
        this.maxX = maxX
        this.maxY = maxY
    }

    open fun set(xOffset: Float, yOffset: Float): Floating {
        this.xOffset = xOffset
        this.yOffset = yOffset
        return this
    }

    open fun changeRate(changeRate: Int): Floating {
        this.changeRate = changeRate
        return this
    }

    open fun randomXY(): Floating {
        this.xOffset = Mathf.random(minX, maxX)
        this.yOffset = Mathf.random(minY, maxY)
        this.xAdding = Mathf.randomBoolean()
        this.yAdding = Mathf.randomBoolean()
        return this
    }

    open fun tryFlipX() {
        val x = xOffset
        if (x <= minX || x >= maxX ||
            (changeRate > 0 && Mathf.random(99) < changeRate)
        ) {
            xAdding = !xAdding
        }
    }

    open fun tryFlipY() {
        val x = yOffset
        if (x <= minY || x >= maxY ||
            (changeRate > 0 && Mathf.random(99) < changeRate)
        ) {
            yAdding = !yAdding
        }
    }

    open fun move(d: Float) {
        if (xAdding)
            xOffset += d
        else
            xOffset -= d
        if (yAdding)
            yOffset += d
        else
            yOffset -= d
        tryFlipX()
        tryFlipY()
    }

    open fun moveX(dx: Float) {
        if (xAdding)
            xOffset += dx
        else
            xOffset -= dx
        tryFlipX()
    }

    open fun moveY(dy: Float) {
        if (yAdding)
            yOffset += dy
        else
            yOffset -= dy
        tryFlipY()
    }
}