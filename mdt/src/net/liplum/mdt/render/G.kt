package net.liplum.mdt.render

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.QuadTree
import arc.math.geom.Rect
import arc.util.Time
import arc.util.Tmp
import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.game.EventType
import mindustry.gen.Building
import mindustry.gen.Buildingc
import mindustry.gen.Icon
import mindustry.graphics.Drawf
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.Tile
import net.liplum.annotations.Subscribe
import net.liplum.lib.arc.darken
import net.liplum.lib.assets.TR
import net.liplum.lib.math.*
import net.liplum.mdt.utils.*

/**
 * G means graphics.
 */
object G {
    var sin = 0f
    var tan = 0f
    /**
     * This should be called before drawing.
     */
    @JvmStatic
    @Subscribe(EventType.Trigger.preDraw)
    fun init() {
        sin = Mathf.absin(Time.time, 6f, 1f)
        tan = Mathf.tan(Time.time, 6f, 1f)
    }
    @JvmStatic
    val TR.realWidth: Float
        get() = width * Draw.scl * Draw.xscl
    @JvmStatic
    val TR.realHeight: Float
        get() = width * Draw.scl * Draw.yscl
    @JvmStatic
    val sclx: Float
        get() = Draw.scl * Draw.xscl
    @JvmStatic
    val scly: Float
        get() = Draw.scl * Draw.yscl
    @JvmStatic
    @JvmOverloads
    fun dashLineBetweenTwoBlocksBreath(
        startTile: Tile, endTile: Tile,
        lineColor: Color = Pal.placing, outlineColor: Color = Pal.gray,
        alpha: Float? = null,
    ) = dashLineBetweenTwoBlocksBreath(
        startTile.block(), startTile.x, startTile.y,
        endTile.block(), endTile.x, endTile.y,
        lineColor, outlineColor, alpha
    )
    @JvmStatic
    @JvmOverloads
    fun dashLineBetweenTwoBlocksBreath(
        startBlock: Block, startBlockX: TileXYs, startBlockY: TileXYs,
        endBlock: Block, endBlockX: TileXYs, endBlockY: TileXYs,
        lineColor: Color = Pal.placing, outlineColor: Color = Pal.gray,
        alpha: Float? = null,
    ) {
        val startDrawX = startBlock.toCenterWorldXY(startBlockX)
        val startDrawY = startBlock.toCenterWorldXY(startBlockY)
        val endDrawX = endBlock.toCenterWorldXY(endBlockX)
        val endDrawY = endBlock.toCenterWorldXY(endBlockY)
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
    fun arrowBetweenTwoBlocksBreath(
        startTile: Tile, pointTile: Tile,
        alpha: Float? = null,
    ) = arrowBetweenTwoBlocksBreath(
        startTile.block(), startTile.x, startTile.y,
        pointTile.block(), pointTile.x, pointTile.y,
        alpha = alpha
    )
    @JvmStatic
    @JvmOverloads
    fun arrowBetweenTwoBlocksBreath(
        startTile: Tile, pointedTile: Tile,
        arrowColor: Color,
        alpha: Float? = null,
    ) = arrowBetweenTwoBlocksBreath(
        startTile.block(), startTile.x, startTile.y, pointedTile.block(), pointedTile.x, pointedTile.y,
        arrowColor, alpha
    )
    @JvmStatic
    @JvmOverloads
    fun arrowBetweenTwoBlocksBreath(
        startBlock: Block, startBlockX: TileXYs, startBlockY: TileXYs,
        pointedBlock: Block, pointedBlockX: TileXYs, pointedBlockY: TileXYs,
        arrowColor: Color = Pal.accent,
        alpha: Float? = null,
    ) {
        val startDrawX = pointedBlock.toCenterWorldXY(pointedBlockX)
        val startDrawY = pointedBlock.toCenterWorldXY(pointedBlockY)
        val pointedDrawX = startBlock.toCenterWorldXY(startBlockX)
        val pointedDrawY = startBlock.toCenterWorldXY(startBlockY)
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
    fun arrowPointingThisBreath(
        pointedBlock: Block, pointedBlockX: TileXYs, pointedBlockY: TileXYs,
        degrees: Float,
        arrowColor: Color = Pal.power,
        alpha: Float? = null,
    ) {
        val pointedDrawX = pointedBlock.toCenterWorldXY(pointedBlockX)
        val pointedDrawY = pointedBlock.toCenterWorldXY(pointedBlockY)
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
        x: WorldXY, y: WorldXY, x2: WorldXY, y2: WorldXY,
        length: Float, radius: Float,
        color: Color = Pal.power,
        alpha: Float? = null,
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
    fun transferArrowLineBreath(
        startDrawX: WorldXY, startDrawY: WorldXY,
        endDrawX: WorldXY, endDrawY: WorldXY,
        arrowColor: Color = Pal.power,
        density: Float = 15f,
        /** unit per tick */
        speed: Float = 60f,
        alpha: Float? = null,
    ) {
        if (density.isZero)
            return
        if (alpha != null && alpha <= 0f)
            return
        val t = Tmp.v2.set(endDrawX, endDrawY)
            .sub(startDrawX, startDrawY)
        val angle = t.angle()
        val length = t.len()
        val count = (Mathf.ceil(length / density)).coerceAtLeast(1)
        val alphaMulti = alpha ?: 1f
        val inner = Tmp.c1.set(arrowColor).a(arrowColor.a * alphaMulti)
        val outline = Tmp.c2.set(arrowColor).a(arrowColor.a * alphaMulti).darken(0.3f)
        var size = 1f + sin * 0.15f
        var outlineSize = 1f + sin * 0.15f + 0.4f
        if (Vars.mobile) {
            size *= 0.6f
            outlineSize *= 0.76f
        }
        val time = length / speed * 60f
        val moving = if (speed > 0f) Tmp.v3.set(t).setLength((length * (Time.time % time / time)) % length)
        else Tmp.v3.set(0f, 0f)
        val cur = Tmp.v4.set(
            startDrawX + moving.x,
            startDrawY + moving.y
        )
        val per = t.scl(1f / count)
        for (i in 0 until count) {
            val line = Tmp.v5.set(cur).sub(startDrawX, startDrawY)
            val lineLength = (line.len() % length / length).smooth * length
            line.setLength(lineLength)
            line.add(startDrawX, startDrawY)
            val fadeAlpha = when {
                lineLength <= 10f -> (lineLength / 10f).smooth
                length - lineLength <= 10f -> ((length - lineLength) / 10f).smooth
                else -> 1f
            }
            Draw.color(outline)
            Icon.right.region.DrawSize(line.x, line.y, size = outlineSize * fadeAlpha, rotation = angle)
            Draw.color(inner)
            Icon.right.region.DrawSize(line.x, line.y, size = size * fadeAlpha, rotation = angle)
            cur += per
        }
        Draw.color()
    }
    @JvmStatic
    @JvmOverloads
    fun transferArrowLineBreath(
        startBlock: Block,
        startBlockX: TileXYs, startBlockY: TileXYs,
        endBlock: Block,
        endBlockX: TileXYs, endBlockY: TileXYs,
        arrowColor: Color = Pal.power,
        density: Float = 15f,
        speed: Float = 60f,
        alpha: Float? = null,
    ) {
        transferArrowLineBreath(
            startBlock.toCenterWorldXY(startBlockX),
            startBlock.toCenterWorldXY(startBlockY),
            endBlock.toCenterWorldXY(endBlockX),
            endBlock.toCenterWorldXY(endBlockY),
            arrowColor,
            density,
            speed,
            alpha,
        )
    }
    @JvmStatic
    @JvmOverloads
    fun transferArrowLineBreath(
        start: Buildingc,
        end: Buildingc,
        arrowColor: Color = Pal.power,
        density: Float = 15f,
        speed: Float = 60f,
        alpha: Float? = null,
    ) = transferArrowLineBreath(
        start.x, start.y,
        end.x, end.y,
        arrowColor, density, speed,
        alpha,
    )
    @JvmStatic
    @JvmOverloads
    fun arrowLineBreath(
        startDrawX: WorldXY, startDrawY: WorldXY,
        endDrawX: WorldXY, endDrawY: WorldXY,
        blockSize: Int,
        density: Float = 15f,
        arrowColor: Color = Pal.power,
        alpha: Float? = null,
    ) {
        if (density.isZero)
            return
        if (alpha != null && alpha <= 0f)
            return
        val t = Tmp.v2.set(endDrawX, endDrawY)
            .sub(startDrawX, startDrawY)
        val angle = t.angle()
        val length = (t.len() - blockSize.worldXY).coerceAtLeast(0f)
        val count = (Mathf.ceil(length / density)).coerceAtLeast(1)
        val per = t.scl(1f / count)
        var curX = startDrawX + per.x
        var curY = startDrawY + per.y
        val alphaMulti = alpha ?: 1f
        val inner = Tmp.c1.set(arrowColor).a(arrowColor.a * alphaMulti)
        val outline = Tmp.c2.set(arrowColor).a(arrowColor.a * alphaMulti).darken(0.3f)
        val size = 1f + sin * 0.15f
        if (count == 1) {
            t.set(startDrawX, startDrawY).add(endDrawX, endDrawY)
            t /= 2f
            Draw.color(outline)
            Icon.right.region.DrawSize(t.x, t.y, size = size + 0.4f, rotation = angle)
            Draw.color(inner)
            Icon.right.region.DrawSize(t.x, t.y, size = size, rotation = angle)
        } else {
            for (i in 0 until count - 1) {
                Draw.color(outline)
                Icon.right.region.DrawSize(curX, curY, size = size + 0.4f, rotation = angle)
                Draw.color(inner)
                Icon.right.region.DrawSize(curX, curY, size = size, rotation = angle)
                curX += per.x
                curY += per.y
            }
        }
        Draw.color()
    }
    @JvmStatic
    @JvmOverloads
    fun arrowLineBreath(
        startBlockX: TileXYs, startBlockY: TileXYs,
        endBlockX: TileXYs, endBlockY: TileXYs,
        density: Float = 15f,
        arrowColor: Color = Pal.power,
        alpha: Float? = null,
    ) {
        arrowLineBreath(
            startBlockX.worldXY,
            startBlockY.worldXY,
            endBlockX.worldXY,
            endBlockY.worldXY,
            2,
            density,
            arrowColor,
            alpha,
        )
    }
    @JvmStatic
    @JvmOverloads
    fun arrowLineBreath(
        startBlock: Block,
        startBlockX: TileXYs, startBlockY: TileXYs,
        endBlock: Block,
        endBlockX: TileXYs, endBlockY: TileXYs,
        density: Float,
        arrowColor: Color = Pal.power,
        alpha: Float? = null,
    ) {
        arrowLineBreath(
            startBlock.toCenterWorldXY(startBlockX),
            startBlock.toCenterWorldXY(startBlockY),
            endBlock.toCenterWorldXY(endBlockX),
            endBlock.toCenterWorldXY(endBlockY),
            startBlock.size,
            density,
            arrowColor,
            alpha,
        )
    }
    @JvmStatic
    @JvmOverloads
    fun arrowLineBreath(
        start: Building,
        end: Building,
        density: Float = 15f,
        arrowColor: Color = Pal.power,
        alpha: Float? = null,
    ) = arrowLineBreath(
        start.x, start.y,
        end.x, end.y,
        start.block.size,
        density, arrowColor,
        alpha,
    )
    @JvmStatic
    @JvmOverloads
    fun circleBreath(
        x: WorldXY, y: WorldXY, rad: Float, color: Color = Pal.power,
        alpha: Float? = null, stroke: Float = 1f,
    ) = circle(x, y, rad + sin, color, alpha, stroke)
    @JvmStatic
    @JvmOverloads
    fun circle(
        x: WorldXY, y: WorldXY, rad: Float, color: Color = Pal.power,
        alpha: Float? = null, stroke: Float = 1f,
    ) {
        Lines.stroke(stroke + 2f, Pal.gray)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Lines.circle(x, y, rad)

        Lines.stroke(stroke, color)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Lines.circle(x, y, rad)
        Draw.reset()
    }
    @JvmStatic
    @JvmOverloads
    fun surroundingCircleBreath(
        t: Tile, circleColor: Color = Pal.power,
        alpha: Float? = null, stroke: Float = 1f,
    ) = circle(
        t.drawx(), t.drawy(),
        (t.block().size / 2f + 1) * Vars.tilesize + sin - 2f,
        circleColor, alpha, stroke
    )
    @JvmStatic
    @JvmOverloads
    fun surroundingCircleBreath(
        b: Block, x: WorldXY, y: WorldXY, circleColor: Color = Pal.power,
        alpha: Float? = null, stroke: Float = 1f,
    ) = circle(
        x, y,
        (b.size / 2f + 1) * Vars.tilesize + sin - 2f,
        circleColor, alpha, stroke
    )
    @JvmStatic
    @JvmOverloads
    fun surroundingCircleBreath(
        b: Block, x: TileXY, y: TileXY,
        circleColor: Color = Pal.power,
        alpha: Float? = null, stroke: Float = 1f,
    ) = circle(
        b.toCenterWorldXY(x),
        b.toCenterWorldXY(y),
        (b.size / 2f + 1) * Vars.tilesize + sin - 2f,
        circleColor, alpha, stroke
    )
    @JvmStatic
    @JvmOverloads
    fun dashCircle(
        x: WorldXY, y: WorldXY, rad: WorldXY, color: Color = Pal.power,
        alpha: Float? = null, stroke: Float = 1f,
    ) {
        Lines.stroke(stroke + 2f, Pal.gray)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Lines.dashCircle(x, y, rad)

        Lines.stroke(stroke, color)
        if (alpha != null) {
            Draw.alpha(alpha)
        }
        Lines.dashCircle(x, y, rad)
        Draw.reset()
    }
    @JvmStatic
    @JvmOverloads
    fun dashCircleBreath(
        b: Block, x: TileXY, y: TileXY, rad: WorldXY,
        circleColor: Color = Pal.power,
        alpha: Float? = null, stroke: Float = 1f,
    ) = dashCircle(
        b.toCenterWorldXY(x),
        b.toCenterWorldXY(y),
        rad + sin - 2f,
        circleColor, alpha, stroke
    )
    @JvmStatic
    @JvmOverloads
    fun dashCircleBreath(
        x: WorldXY, y: WorldXY, rad: WorldXY,
        color: Color = Pal.power,
        alpha: Float? = null, stroke: Float = 1f,
    ) = dashCircle(
        x, y, rad + sin - 2f,
        color, alpha, stroke
    )
    @JvmStatic
    @JvmOverloads
    fun dashCircleBreath(
        build: Building,
        range: WorldXY, color: Color = Pal.power,
        alpha: Float? = null, stroke: Float = 1f,
    ) = dashCircle(build.x, build.y, range + sin - 2, color, alpha, stroke)
    @JvmStatic
    @JvmOverloads
    fun dashCircleBreath(
        b: Block, blockX: TileXYs, BlockY: TileXYs,
        range: WorldXY, color: Color = Pal.power,
        alpha: Float? = null, stroke: Float = 1f,
    ) = dashCircle(
        b.toCenterWorldXY(blockX),
        b.toCenterWorldXY(BlockY),
        range + sin - 2, color, alpha, stroke
    )
    @JvmStatic
    @JvmOverloads
    fun selectedBreath(other: Building, color: Color = Pal.power, temp: Color = Tmp.c1) {
        Drawf.selected(
            other,
            temp.set(color).a(Mathf.absin(4f, 1f))
        )
    }
    @JvmStatic
    @JvmOverloads
    fun selected(other: Building, color: Color = Pal.power) {
        Drawf.selected(other, color)
    }
    @JvmStatic
    @JvmOverloads
    fun materialIcon(
        b: Building, material: UnlockableContent,
        alpha: Float = 1f,
    ) {
        val dx = b.x - b.block.size * Vars.tilesize / 2f
        val dy = b.y + b.block.size * Vars.tilesize / 2f
        val size = Vars.iconSmall / 4f
        val icon = material.fullIcon
        Draw.mixcol(Color.darkGray, 1f)
        Draw.alpha(alpha)
        Draw.rect(icon, dx, dy - 1, size, size)
        Draw.reset()
        Draw.alpha(alpha)
        Draw.rect(icon, dx, dy, size, size)
    }
    @JvmStatic
    @JvmOverloads
    fun materialIcons(
        b: Building, materials: Iterable<UnlockableContent>,
        alpha: Float = 1f, maxPerRow: Int = 4,
    ) {
        val dx = b.x - b.block.size * Vars.tilesize / 2f
        val dy = b.y + b.block.size * Vars.tilesize / 2f
        val size = Vars.iconSmall / 4f
        for ((i, material) in materials.withIndex()) {
            val icon = material.fullIcon
            Draw.mixcol(Color.darkGray, 1f)
            Draw.alpha(alpha)
            val x = dx + i % maxPerRow * icon.width / 2f * sclx
            val y = dy - i / maxPerRow * icon.width / 2f * scly
            Draw.rect(icon, x, y - 1, size, size)
            Draw.reset()
            Draw.alpha(alpha)
            Draw.rect(icon, x, y, size, size)
        }
    }
    @JvmStatic
    @JvmOverloads
    fun wrappedSquareBreath(
        b: Building, color: Color = Pal.accent,
    ) {
        Drawf.square(b.x, b.y, b.block.size * Vars.tilesize / 2f + 2.5f + sin, 0f, color)
    }
    @JvmStatic
    @JvmOverloads
    fun wrappedSquare(
        b: Building, color: Color = Pal.accent,
    ) {
        Drawf.square(b.x, b.y, b.block.size * Vars.tilesize / 2f + 2.5f, 0f, color)
    }
    @JvmStatic
    @JvmOverloads
    fun lineBreath(
        x: WorldXY, y: WorldXY, x2: WorldXY, y2: WorldXY, color: Color = Pal.accent,
        alpha: Float? = null, stroke: Float = 1f,
    ) {
        line(x, y, x2, y2, color, alpha, stroke + sin)
    }
    @JvmStatic
    @JvmOverloads
    fun line(
        x: WorldXY, y: WorldXY, x2: WorldXY, y2: WorldXY, color: Color = Pal.accent,
        alpha: Float? = null, stroke: Float = 1f,
    ) {
        Lines.stroke(stroke + 2f)
        Draw.color(Pal.gray, color.a)
        if (alpha != null) Draw.alpha(alpha)
        Lines.line(x, y, x2, y2)
        Lines.stroke(stroke, color)
        if (alpha != null) Draw.alpha(alpha)
        Lines.line(x, y, x2, y2)
        Draw.reset()
    }
    @JvmStatic
    @JvmOverloads
    fun rect(
        x: WorldXY, y: WorldXY, width: WorldXY, height: WorldXY,
        color: Color = Pal.accent, alpha: Float? = null, stroke: Float = 1f,
    ) {
        line(x, y, x + width, y, color, alpha, stroke)
        line(x + width, y, x + width, y + height, color, alpha, stroke)
        line(x + width, y + height, x, y + height, color, alpha, stroke)
        line(x, y + height, x, y, color, alpha, stroke)
    }
    @JvmStatic
    @JvmOverloads
    fun rect(
        rect: Rect,
        color: Color = Pal.accent, alpha: Float? = null, stroke: Float = 1f,
    ) {
        rect(rect.x, rect.y, rect.width, rect.height, color, alpha, stroke)
    }
    @JvmStatic
    @JvmOverloads
    fun rect(
        hitboxEntity: QuadTree.QuadTreeObject,
        color: Color = Pal.accent, alpha: Float? = null, stroke: Float = 1f,
    ) {
        hitboxEntity.hitbox(Tmp.r1)
        val rect = Tmp.r1
        rect(rect.x, rect.y, rect.width, rect.height, color, alpha, stroke)
    }
}