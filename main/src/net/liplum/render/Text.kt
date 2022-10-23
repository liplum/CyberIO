package net.liplum.render

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Font
import arc.graphics.g2d.GlyphLayout
import arc.graphics.g2d.Lines
import arc.scene.ui.layout.Scl
import arc.util.Align
import arc.util.pooling.Pools
import mindustry.Vars
import mindustry.graphics.Pal
import mindustry.ui.Fonts
import plumy.dsl.WorldXY

object Text {
    /**
     * Draw text and underline in default size
     * @return the width of text
     */
    fun drawUnderlineText(
        text: String,
        x: WorldXY,
        y: WorldXY,
        color: Color = Pal.accent,
    ): WorldXY {
        if (Vars.renderer.pixelator.enabled()) return 0f
        val font = Fonts.outline
        val layout = Pools.obtain(GlyphLayout::class.java, ::GlyphLayout)
        val ints = font.usesIntegerPositions()
        font.setUseIntegerPositions(false)
        font.data.setScale(1f / 4f / Scl.scl(1f))
        layout.setText(font, text)
        val width = layout.width
        font.color = color
        val dx = x
        var dy = y
        font.draw(text, dx, dy + layout.height + 1, Align.center)
        // Underline
        dy -= 1f
        Lines.stroke(2f, Color.darkGray)
        Lines.line(dx - layout.width / 2f - 2f, dy, dx + layout.width / 2f + 1.5f, dy)
        Lines.stroke(1f, color)
        Lines.line(dx - layout.width / 2f - 2f, dy, dx + layout.width / 2f + 1.5f, dy)
        font.setUseIntegerPositions(ints)
        font.color = Color.white
        font.data.setScale(1f)
        Draw.reset()
        Pools.free(layout)
        return width
    }
    /**
     * Draw text and underline in default size
     * @return the width of text
     */
    fun drawTextEasy(
        text: String,
        x: WorldXY,
        y: WorldXY,
        color: Color = Pal.accent,
        scale: Float = 1f,
    ): WorldXY {
        if (Vars.renderer.pixelator.enabled()) return 0f
        val font = Fonts.outline
        val layout = Pools.obtain(GlyphLayout::class.java, ::GlyphLayout)
        val ints = font.usesIntegerPositions()
        font.setUseIntegerPositions(false)
        font.data.setScale(1f / 4f / Scl.scl(scale))
        layout.setText(font, text)
        val width = layout.width
        font.color = color
        font.draw(text, x, y + layout.height, Align.center)
        font.setUseIntegerPositions(ints)
        font.color = Color.white
        font.data.setScale(1f)
        Draw.reset()
        Pools.free(layout)
        return width
    }
    /**
     * Draw text in default size
     * @return the width of text
     */
    inline fun drawText(func: GlyphLayout.(Font) -> Unit): WorldXY {
        if (Vars.renderer.pixelator.enabled()) return 0f
        val font = Fonts.outline
        val layout = Pools.obtain(GlyphLayout::class.java, ::GlyphLayout)
        val ints = font.usesIntegerPositions()
        font.setUseIntegerPositions(false)
        font.data.setScale(1f / 4f / Scl.scl(1f))
        layout.func(font)
        val width = layout.width
        font.setUseIntegerPositions(ints)
        font.color = Color.white
        font.data.setScale(1f)
        Draw.reset()
        Pools.free(layout)
        return width
    }
    /**
     * @return the width of text
     */
    inline fun drawTextX(func: GlyphLayout.(Font) -> Unit): WorldXY {
        if (Vars.renderer.pixelator.enabled()) return 0f
        val font = Fonts.outline
        val layout = Pools.obtain(GlyphLayout::class.java, ::GlyphLayout)
        val ints = font.usesIntegerPositions()
        font.setUseIntegerPositions(false)
        layout.func(font)
        val width = layout.width
        font.setUseIntegerPositions(ints)
        font.color = Color.white
        font.data.setScale(1f)
        Draw.reset()
        Pools.free(layout)
        return width
    }
}