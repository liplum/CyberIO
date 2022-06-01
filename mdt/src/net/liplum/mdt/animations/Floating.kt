package net.liplum.mdt.animations

import arc.math.Mathf

open class Floating {
    val minX: Float
    val minY: Float
    val maxX: Float
    val maxY: Float
    var changeRate = 0
    var dx = 0f
        set(value) {
            field = value.coerceIn(minX, maxX)
        }
    var dy = 0f
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
        this.dx = xOffset
        this.dy = yOffset
        return this
    }

    open fun changeRate(changeRate: Int): Floating {
        this.changeRate = changeRate
        return this
    }

    open fun randomXY(): Floating {
        this.dx = Mathf.random(minX, maxX)
        this.dy = Mathf.random(minY, maxY)
        this.xAdding = Mathf.randomBoolean()
        this.yAdding = Mathf.randomBoolean()
        return this
    }

    open fun tryFlipX() {
        val x = dx
        if (x <= minX || x >= maxX ||
            (changeRate > 0 && Mathf.random(99) < changeRate)
        ) {
            xAdding = !xAdding
        }
    }

    open fun tryFlipY() {
        val x = dy
        if (x <= minY || x >= maxY ||
            (changeRate > 0 && Mathf.random(99) < changeRate)
        ) {
            yAdding = !yAdding
        }
    }

    open fun move(d: Float) {
        if (xAdding)
            dx += d
        else
            dx -= d
        if (yAdding)
            dy += d
        else
            dy -= d
        tryFlipX()
        tryFlipY()
    }

    open fun moveX(dx: Float) {
        if (xAdding)
            this.dx += dx
        else
            this.dx -= dx
        tryFlipX()
    }

    open fun moveY(dy: Float) {
        if (yAdding)
            this.dy += dy
        else
            this.dy -= dy
        tryFlipY()
    }
}