package net.liplum.common.ui

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table

class ProgressTable : Table() {
    var progress: () -> Float = { 1f }
    var progressOmit = 0f
    var progressColor = Color()
    override fun drawBackground(x: Float, y: Float) {
        if (background == null) return
        val color = progressColor
        Draw.color(color.r, color.g, color.b, color.a * parentAlpha)
        val p = progress().coerceIn(0f, 1f)
        if (p > 0f && p > progressOmit) {
            val width = width * p
            background.draw(x, y, width, height)
        }
    }
}

fun Table.addProgressTable(func: ProgressTable.() -> Unit): Cell<ProgressTable> =
    this.add(ProgressTable().apply(func))
