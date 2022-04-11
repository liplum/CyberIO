@file:Suppress("SpellCheckingInspection")

package net.liplum.utils

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.graphics.g2d.TextureRegion
import arc.math.Angles
import arc.math.Mathf
import arc.util.Time
import arc.util.Tmp
import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.Tile

object G {
    var sin = 0f
    var tan = 0f
    @JvmStatic
    fun Ax(): Float {
        return Draw.scl * Draw.xscl
    }
    @JvmStatic
    fun Ay(): Float {
        return Draw.scl * Draw.yscl
    }
    @JvmStatic
    fun Dw(tr: TextureRegion): Float {
        return D(tr.width.toFloat())
    }
    @JvmStatic
    fun Dh(tr: TextureRegion): Float {
        return D(tr.height.toFloat())
    }
    @JvmStatic
    fun D(a: Float): Float {
        return a * Draw.scl * Draw.xscl
    }
    @JvmStatic
    fun D(a: Int): Float {
        return a * Draw.scl * Draw.xscl
    }
    @JvmStatic
    fun init() {
        sin = Mathf.absin(Time.time, 6f, 1f)
        tan = Mathf.tan(Time.time, 6f, 1f)
    }
    @JvmStatic
    @JvmOverloads
    fun drawDashLineBetweenTwoBlocks(
        startTile: Tile, endTile: Tile,
        lineColor: Color = Pal.placing, outlineColor: Color = Pal.gray,
        alpha: Float? = null
    ) = drawDashLineBetweenTwoBlocks(
        startTile.block(), startTile.x, startTile.y,
        endTile.block(), endTile.x, endTile.y,
        lineColor, outlineColor, alpha
    )
    @JvmStatic
    @JvmOverloads
    fun drawDashLineBetweenTwoBlocks(
        startBlock: Block, startBlockX: Short, startBlockY: Short,
        endBlock: Block, endBlockX: Short, endBlockY: Short,
        lineColor: Color = Pal.placing, outlineColor: Color = Pal.gray,
        alpha: Float? = null
    ) {
        val startDrawX = startBlockX.toDrawXY(startBlock)
        val startDrawY = startBlockY.toDrawXY(startBlock)
        val endDrawX = endBlockX.toDrawXY(endBlock)
        val endDrawY = endBlockY.toDrawXY(endBlock)
        val segsf = distance(
            startDrawX,
            startDrawY,
            (endBlockX * Vars.tilesize).toFloat(),
            (endBlockY * Vars.tilesize).toFloat()
        ) / Vars.tilesize
        Tmp.v1.set(endDrawX, endDrawY)
            .sub(startDrawX, startDrawY)
            .limit((endBlock.size / 2f + 1) * Vars.tilesize + sin + 0.5f)
        val x2 = endBlockX * Vars.tilesize - Tmp.v1.x
        val y2 = endBlockY * Vars.tilesize - Tmp.v1.y
        val x1 = startDrawX + Tmp.v1.x
        val y1 = startDrawY + Tmp.v1.y
        val segs = segsf.toInt()
        Lines.stroke(4f, outlineColor)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Lines.dashLine(x1, y1, x2, y2, segs)

        Lines.stroke(2f, lineColor)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Lines.dashLine(x1, y1, x2, y2, segs)

        Draw.reset()
    }
    @JvmStatic
    @JvmOverloads
    fun drawArrowBetweenTwoBlocks(
        startTile: Tile, pointTile: Tile,
        alpha: Float? = null
    ) = drawArrowBetweenTwoBlocks(
        startTile.block(), startTile.x, startTile.y,
        pointTile.block(), pointTile.x, pointTile.y,
        alpha = alpha
    )
    @JvmStatic
    @JvmOverloads
    fun drawArrowBetweenTwoBlocks(
        startTile: Tile, pointedTile: Tile,
        arrowColor: Color,
        alpha: Float? = null
    ) = drawArrowBetweenTwoBlocks(
        startTile.block(), startTile.x, startTile.y, pointedTile.block(), pointedTile.x, pointedTile.y,
        arrowColor, alpha
    )
    @JvmOverloads
    @JvmStatic
    fun drawArrowBetweenTwoBlocks(
        startBlock: Block, startBlockX: Short, startBlockY: Short,
        pointedBlock: Block, pointedBlockX: Short, pointedBlockY: Short,
        arrowColor: Color = Pal.accent,
        alpha: Float? = null
    ) {
        val startDrawX = pointedBlockX.toDrawXY(pointedBlock)
        val startDrawY = pointedBlockY.toDrawXY(pointedBlock)
        val pointedDrawX = startBlockX.toDrawXY(startBlock)
        val pointedDrawY = startBlockY.toDrawXY(startBlock)
        arrow(
            pointedDrawX, pointedDrawY,
            startDrawX, startDrawY,
            startBlock.size * Vars.tilesize + sin,
            4f + sin,
            arrowColor, alpha
        )
    }
    @JvmStatic
    @JvmOverloads
    fun drawArrowPointingThis(
        pointedBlock: Block, pointedBlockX: Short, pointedBlockY: Short,
        degrees: Float,
        arrowColor: Color,
        alpha: Float? = null
    ) {
        val pointedDrawX = pointedBlockX.toDrawXY(pointedBlock)
        val pointedDrawY = pointedBlockY.toDrawXY(pointedBlock)
        Tmp.v2.set(1f, 1f).setAngle(degrees).setLength(22f)
        arrow(
            pointedDrawX + Tmp.v2.x, pointedDrawY + Tmp.v2.y,
            pointedDrawX, pointedDrawY,
            pointedBlock.size * Vars.tilesize + sin,
            4f + sin,
            arrowColor, alpha
        )
    }
    @JvmStatic
    @JvmOverloads
    fun arrow(
        x: Float, y: Float, x2: Float, y2: Float,
        length: Float, radius: Float,
        color: Color,
        alpha: Float? = null
    ) {
        val angle = Angles.angle(x, y, x2, y2)
        val space = 2f
        Tmp.v1.set(x2, y2).sub(x, y).limit(length)
        val vx = Tmp.v1.x + x
        val vy = Tmp.v1.y + y
        Draw.color(Pal.gray)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Fill.poly(vx, vy, 3, radius + space, angle)
        Draw.color(color)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Fill.poly(vx, vy, 3, radius, angle)
        Draw.color()
    }
    @JvmStatic
    @JvmOverloads
    fun drawArrowLine(
        startDrawX: Float, startDrawY: Float,
        endDrawX: Float, endDrawY: Float,
        blockSize: Int,
        density: Float, arrowColor: Color,
        alpha: Float? = null
    ) {
        val T = Tmp.v2.set(endDrawX, endDrawY).sub(startDrawX, startDrawY)
        val length = T.len()
        val count = Mathf.ceil(length / density)
        val per = T.scl(1f / count)
        var curX = startDrawX
        var curY = startDrawY
        for (i in 1 until count) {
            arrow(
                curX,
                curY,
                curX + per.x,
                curY + per.y,
                blockSize * Vars.tilesize + sin,
                4f + sin,
                arrowColor, alpha
            )
            curX += per.x
            curY += per.y
        }
    }
    @JvmStatic
    @JvmOverloads
    fun drawArrowLine(
        startBlockX: Short, startBlockY: Short,
        endBlockX: Short, endBlockY: Short,
        density: Float, arrowColor: Color,
        alpha: Float? = null
    ) {
        drawArrowLine(
            startBlockX.toDrawXY,
            startBlockY.toDrawXY,
            endBlockX.toDrawXY,
            endBlockY.toDrawXY,
            2, density, arrowColor,
            alpha
        )
    }
    @JvmStatic
    @JvmOverloads
    fun drawArrowLine(
        startBlock: Block,
        startBlockX: Short, startBlockY: Short,
        endBlock: Block,
        endBlockX: Short, endBlockY: Short,
        density: Float, arrowColor: Color,
        alpha: Float? = null
    ) {
        drawArrowLine(
            startBlockX.toDrawXY(startBlock),
            startBlockY.toDrawXY(startBlock),
            endBlockX.toDrawXY(endBlock),
            endBlockY.toDrawXY(endBlock),
            startBlock.size, density, arrowColor,
            alpha
        )
    }
    @JvmStatic
    @JvmOverloads
    fun drawArrowLine(
        start: Building,
        end: Building,
        density: Float,
        arrowColor: Color,
        alpha: Float? = null
    ) = drawArrowLine(
        start.x, start.y,
        end.x, end.y,
        start.block.size, density, arrowColor,
        alpha
    )
    @JvmStatic
    @JvmOverloads
    fun circle(
        x: Float, y: Float, rad: Float, color: Color,
        alpha: Float? = null, storke: Float = 1f
    ) {
        Lines.stroke(storke + 2f, Pal.gray)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Lines.circle(x, y, rad)

        Lines.stroke(storke, color)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Lines.circle(x, y, rad)
        Draw.reset()
    }
    @JvmStatic
    @JvmOverloads
    fun drawSurroundingCircle(
        t: Tile, circleColor: Color,
        alpha: Float? = null, storke: Float = 1f
    ) = circle(
        t.drawx(), t.drawy(),
        (t.block().size / 2f + 1) * Vars.tilesize + sin - 2f,
        circleColor, alpha, storke
    )
    @JvmStatic
    @JvmOverloads
    fun drawSurroundingCircle(
        b: Block, drawX: Float, drawY: Float, circleColor: Color,
        alpha: Float? = null, storke: Float = 1f
    ) = circle(
        drawX, drawY,
        (b.size / 2f + 1) * Vars.tilesize + sin - 2f,
        circleColor, alpha, storke
    )
    @JvmStatic
    @JvmOverloads
    fun drawSurroundingCircle(
        b: Block, worldX: Int, worldY: Int,
        circleColor: Color,
        alpha: Float? = null, storke: Float = 1f
    ) = circle(
        worldX.toDrawXY(b),
        worldY.toDrawXY(b),
        (b.size / 2f + 1) * Vars.tilesize + sin - 2f,
        circleColor, alpha, storke
    )
    @JvmStatic
    @JvmOverloads
    fun dashCircle(
        x: Float, y: Float, rad: Float, color: Color,
        alpha: Float? = null, storke: Float = 1f
    ) {
        Lines.stroke(storke + 2f, Pal.gray)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Lines.dashCircle(x, y, rad)

        Lines.stroke(storke, color)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Lines.dashCircle(x, y, rad)
        Draw.reset()
    }
    @JvmStatic
    @JvmOverloads
    fun drawDashCircle(
        build: Building,
        range: Float, color: Color,
        alpha: Float? = null, storke: Float = 1f
    ) = dashCircle(build.x, build.y, range + sin - 2, color, alpha, storke)
    @JvmStatic
    @JvmOverloads
    fun drawDashCircle(
        x: Float, y: Float,
        range: Float, color: Color,
        alpha: Float? = null, storke: Float = 1f
    ) = dashCircle(x, y, range + sin - 2, color, alpha, storke)
    @JvmStatic
    @JvmOverloads
    fun drawDashCircle(
        b: Block, blockX: Short, BlockY: Short,
        range: Float, color: Color,
        alpha: Float? = null, storke: Float = 1f
    ) = dashCircle(
        blockX.toDrawXY(b),
        BlockY.toDrawXY(b),
        range + sin - 2, color, alpha, storke
    )
    @JvmStatic
    @JvmOverloads
    fun drawSelected(other: Building, color: Color, temp: Color = Tmp.c1) {
        Drawf.selected(
            other,
            temp.set(color).a(Mathf.absin(4f, 1f))
        )
    }
    @JvmStatic
    fun drawMaterialIcon(
        b: Building, material: UnlockableContent,
        alpha: Float = 1f
    ) {
        val dx = b.x - b.block.size * Vars.tilesize / 2f
        val dy = b.y + b.block.size * Vars.tilesize / 2f
        Draw.mixcol(Color.darkGray, alpha)
        Draw.rect(material.uiIcon, dx, dy - 1)
        Draw.reset()
        Draw.alpha(alpha)
        Draw.rect(material.uiIcon, dx, dy)
    }
    @JvmStatic
    fun drawMaterialIcons(
        b: Building, materials: Array<out UnlockableContent>,
        alpha: Float = 1f, maxPerRow: Int = 4
    ) {
        val dx = b.x - b.block.size * Vars.tilesize / 2f
        val dy = b.y + b.block.size * Vars.tilesize / 2f
        for (i in materials.indices) {
            val material = materials[i]
            val uiIcon = material.uiIcon
            Draw.mixcol(Color.darkGray, alpha)
            val x = dx + i % maxPerRow * D(uiIcon.width)
            val y = dy - i / maxPerRow * D(uiIcon.height)
            Draw.rect(uiIcon, x, y - 1)
            Draw.reset()
            Draw.alpha(alpha)
            Draw.rect(uiIcon, x, y)
        }
    }
}
