package net.liplum.ui

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.ScissorStack
import arc.math.geom.Rect
import arc.scene.Element
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.Image
import arc.scene.ui.layout.Stack
import arc.scene.ui.layout.Table
import plumy.core.assets.EmptyTR
import plumy.core.assets.TR
import plumy.core.math.isZero

class ItemProgressImage(
    region: TR,
    size: Float = 32f,
    progress: () -> Float,
) : Stack() {
    val img: ProgressImage = ProgressImage(region, progress)

    init {
        add(Table().apply {
            left()
            add(Image(region)).size(size)
        })
        add(Table().apply {
            left().bottom()
            add(img).size(size)
            pack()
        })
    }
}

class ProgressImage(
    image: TR = EmptyTR,
    var progress: () -> Float,
) : Element() {
    var isHorizontal = false
    var topDown = true
    var alpha = 0.6f
    var mask = TextureRegionDrawable(image)
    private val scissor = Rect()
    override fun draw() {
        val p = progress().coerceIn(0f, 1f)
        if (p <= 0) return
        val widthDraw = if (isHorizontal) width * p else width
        val heightDraw = if (isHorizontal) height else height * p
        if ((widthDraw - width).isZero && (heightDraw - height).isZero) {
            drawMask()
            return
        }
        if (topDown)
            scissor.set(x, y, widthDraw, heightDraw)
        else
            scissor.set(x + width - widthDraw, y + height - heightDraw, widthDraw, heightDraw)
        if (ScissorStack.push(scissor)) {
            drawMask()
            ScissorStack.pop()
        }
    }

    fun drawMask() {
        Draw.color(Color.black)
        Draw.alpha(alpha)
        mask.draw(x, y, width, height)
    }
}