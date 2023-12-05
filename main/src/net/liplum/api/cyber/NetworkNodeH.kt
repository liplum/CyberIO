@file:JvmName("DataNetworkH")

package net.liplum.api.cyber

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.geom.Geometry
import arc.util.Time
import arc.util.Tmp
import mindustry.Vars
import mindustry.Vars.world
import mindustry.gen.Iconc
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.api.cyber.SideLinks.Companion.coordinates
import net.liplum.api.cyber.SideLinks.Companion.reflect
import plumy.core.assets.TR
import net.liplum.common.shader.use
import plumy.dsl.DrawLayer
import net.liplum.data.EmptyDataID
import net.liplum.data.PayloadData
import plumy.core.math.Progress
import plumy.core.math.between
import plumy.core.math.pow3InIntrp
import net.liplum.input.Inspector.isPlacing
import net.liplum.input.Inspector.isSelected
import net.liplum.input.smoothPlacing
import net.liplum.input.smoothSelect
import plumy.animation.ContextDraw.Draw
import plumy.animation.ContextDraw.DrawSize
import net.liplum.registry.SD
import net.liplum.render.G
import net.liplum.render.Text
import plumy.dsl.*
import kotlin.math.absoluteValue

/**
 * Update the link in four cardinal directions.
 * This function may add link or remove link.
 * # Add or Remove
 * `Add link` will affect two nodes.
 * `Remove link` will only affect who calls this function.
 */
fun INetworkNode.updateCardinalDirections() = building.run body@{
    val offset = block.size / 2
    val tileX = tile.x.toInt()
    val tileY = tile.y.toInt()
    val range = tileLinkRange
    forEachEnabledSide iterateCurSide@{ side ->
        val pre = links[side]
        val dir = coordinates[side]
        var destination: INetworkNode? = null
        for (j in 1 + offset..range + offset) { // expending
            val b = world.build(tileX + j * dir.x, tileY + j * dir.y)
            if (b != null && b.team == team && b is INetworkNode) {
                destination = b
                break
            }
        }
        val dest = destination
        if (dest != null) { // reach a node
            val destBuild = dest.building
            // if not the previous one
            if (destBuild.pos() != pre) {
                run firstCheck@{
                    // Firstly, if the target has a link on this side(of course, reflected)
                    // check whether this node is closer than the old one target has
                    val targetReflectSideNode = dest.links[side.reflect].castBuild<INetworkNode>()
                    // The current node this target links on the corresponding side
                    if (targetReflectSideNode != null) {
                        // If this node is nearer than the old one target linked, let target link to this.
                        if (destBuild.dst(this) < destBuild.dst(targetReflectSideNode.building)) {
                            if (canLink(side, dest)) {
                                link(side, dest)
                            }// else : reached is invalid -> do nothing
                        }
                        return@iterateCurSide
                    }
                }
                run secondCheck@{
                    // Secondly, if this has a link on this side
                    // check whether the target is closer than the old one.
                    val preNode = pre.castBuild<INetworkNode>()
                    if (preNode != null) {
                        if (destBuild.dst(this) < preNode.building.dst(this)) {
                            if (canLink(side, dest)) {
                                link(side, dest)
                            }// else : reached is invalid -> do nothing
                        }
                    } else {
                        if (canLink(side, dest)) {
                            link(side, dest)
                        }// else : reached is invalid -> do nothing
                    }
                    return@iterateCurSide
                }
            }// is previous one -> do nothing
            return@iterateCurSide
        } else {
            // doesn't reach any node -> clear current side
            clearSide(side)
            return@iterateCurSide
        }
    }
}
/**
 * Get the reflected side index in [Geometry.d4]
 */
fun reflect(original: Int): Int =
    (original + 2) % 4

fun INetworkNode.drawSelectingCardinalDirections() = building.run {
    // Draw a cross
    if (!this.isSelected()) return
    val team = Vars.player.team()
    val range = tileLinkRange
    val size = block.size
    val tileX = tile.x
    val tileY = tile.y
    val offset = size / 2
    for (side in RIGHT..BOTTOM) {
        var maxLen = range.worldXY
        var limit = -1f
        var dest: INetworkNode? = null
        val dir = coordinates[side]
        val dx = dir.x
        val dy = dir.y
        for (j in 1 + offset..range) {
            val other = world.build(tileX + j * dir.x, tileY + j * dir.y)
            if (other != null && other.team == team && other is INetworkNode &&
                this@drawSelectingCardinalDirections.isLinkedWith(side, other)
            ) {
                limit = j.worldXY
                dest = other
                break
            }
            DebugOnly {
                val tile = world.tile(tileX + j * dir.x, tileY + j * dir.y)
                if (tile != null) {
                    Tmp.r1.setCenter(tile.x.worldXY, tile.y.worldXY)
                        .setSize(Vars.tilesize.toFloat())
                    G.rect(Tmp.r1, alpha = 0.2f)
                }
            }
        }
        maxLen *= smoothSelect(expendSelectingLineTime)
        if (limit > 0f) maxLen = maxLen.coerceAtMost(limit)
        val blockOffset = (size / 2f).worldXY
        val x1 = x + dx * blockOffset
        val y1 = y + dy * blockOffset
        val x2 = x + dx * maxLen
        val y2 = y + dy * maxLen
        DebugOnly {
            Text.drawTextEasy(
                "${maxLen.toInt()}",
                x + size * 2 * dir.x,
                y + size * 2 * dir.y,
                R.C.GreenSafe
            )
        }
        if (dest != null) {
            val raycastReach = maxLen >= dest.building.dst(this) - dest.block.size.worldXY
            val color = if (raycastReach) R.C.GreenSafe else Pal.placing
            G.lineBreath(x1, y1, x2, y2, color, stroke = 2f)
            if (raycastReach)
                G.wrappedSquareBreath(dest.building, color = color)
        } else {
            G.lineBreath(x1, y1, x2, y2, Pal.placing, stroke = 2f)
        }
    }
    drawRangeCircle(alpha = smoothSelect(expendSelectingLineTime))
}

fun INetworkBlock.drawPlaceCardinalDirections(
    x: TileXY, y: TileXY,
) = block.run {
    // Draw a cross
    if (!this.isPlacing()) return
    val team = Vars.player.team()
    val range = tileLinkRange
    val offset = size / 2
    for (side in RIGHT..BOTTOM) {
        var maxLen = range.worldXY
        var limit = -1f
        var dest: INetworkNode? = null
        val dir = coordinates[side]
        val dx = dir.x
        val dy = dir.y
        for (j in 1 + offset..range) {
            val other = world.build(x + j * dir.x, y + j * dir.y)
            if (other != null && other.team == team && other is INetworkNode) {
                limit = j.worldXY
                dest = other
                break
            }
        }
        maxLen *= smoothPlacing(expendPlacingLineTime)
        if (limit > 0f) maxLen = maxLen.coerceAtMost(limit)
        val worldX = x.worldXY
        val worldY = y.worldXY
        val blockOffset = (size / 2f).worldXY
        val x1 = worldX + dx * blockOffset
        val y1 = worldY + dy * blockOffset
        val x2 = worldX + dx * maxLen
        val y2 = worldY + dy * maxLen
        DebugOnly {
            Text.drawTextEasy(
                "${maxLen.toInt()}",
                worldX + size * 2 * dir.x,
                worldY + size * 2 * dir.y,
                R.C.GreenSafe
            )
        }
        if (dest != null) {
            val raycastReach = maxLen >= dest.building.dst(worldX, worldY) - dest.block.size.worldXY
            val color = if (raycastReach) R.C.GreenSafe else Pal.placing
            G.lineBreath(x1, y1, x2, y2, color, stroke = 2f)
            if (raycastReach)
                G.wrappedSquareBreath(dest.building, color = color)
        } else {
            G.lineBreath(x1, y1, x2, y2, Pal.placing, stroke = 2f)
        }
    }
    drawRangeCircle(x, y, alpha = smoothPlacing(expendPlacingLineTime))
}
@DebugOnly
fun INetworkNode.drawPayloadList() = building.run {
    DrawLayer(Layer.blockOver) {
        dataList.forEachIndexed { i, it ->
            it.payload.icon().Draw(x - dataList.size * 2f + i * 4f, y + block.size.worldXY, payloadRotation)
        }
    }
}
@DebugOnly
fun INetworkNode.drawLinkInfo() = building.run {
    DrawLayer {
        Draw.z(Layer.overlayUI)
        if (sendingProgress > 0f) {
            val width = 25f
            Fill.rect(x, y, width, 5f)
            Draw.color(Var.Hologram)
            Fill.rect(x - width * (1f - sendingProgress) / 2f, y, width * sendingProgress, 5f)
            Draw.color()
        }
        forEachEnabledSide { side ->
            val link = links[side]
            val linkB = link.build
            if (linkB != null)
                G.dashLineBetweenTwoBlocksBreath(this.tile, linkB.tile, alpha = 0.2f)
        }

        Text.drawTextEasy("${network.id}", x, y + 5f, R.C.RedAlert)
        Text.drawTextEasy("${building.id}", x, y - 7f, R.C.Holo)
        for (side in RIGHT..BOTTOM) {
            val size = block.size.worldXY / 2f
            val dir = coordinates[side]
            val pos = links[side]
            val text = pos.castBuild<INetworkNode>()?.let { "${it.tile.x},${it.tile.y}" } ?: "-1"
            Text.drawTextEasy(
                text,
                x + size * dir.x, y + size * dir.y,
                if (currentOriented == side) R.C.BrainWave else R.C.GreenSafe
            )
        }
        val req = if (request == EmptyDataID) "?" else "$request"
        Text.drawTextEasy(
            req,
            x, y + block.size.worldXY / 3f, R.C.VirusBK
        )
    }
}
@DebugOnly
fun INetworkNode.drawNetworkInfo() = building.run {
    DrawLayer {
        Draw.z(Layer.overlayUI)
        val text: String
        val color: Color
        if (network.entity.isAdded) {
            text = network.nodes.joinToString("\n")
            color = Color.white
        } else {
            text = Iconc.cancel.toString()
            color = R.C.RedAlert
        }
        Text.drawTextEasy(
            text,
            x, y + block.size.worldXY, color
        )
    }
}

fun INetworkNode.drawRangeCircle(alpha: Float) = building.run {
    G.circleBreath(x, y, linkRange, alpha = alpha)
}

fun INetworkBlock.drawRangeCircle(x: TileXY, y: TileXY, alpha: Float) = block.run {
    G.circleBreath(getCenterWorldXY(x), getCenterWorldXY(y), linkRange, alpha = alpha)
}

fun INetworkNode.drawRail(beamTR: TR, beamEndTR: TR) {
    DrawLayer(Layer.blockOver) {
        val thisOffset = block.size * Vars.tilesize / 2f
        val ox = this.building.x
        val oy = this.building.y
        val widthHalf = Var.NetworkNodeRailWidth / 2f
        val thickness = Var.NetworkRailThickness
        Draw.color(Var.Hologram)
        links.forEachNodeWithSide { side, t ->
            val time = linkingTime[side]
            val dir = coordinates[side]
            val tx = t.building.x
            val ty = t.building.y
            val x = (ox + tx) / 2f
            val y = (oy + ty) / 2f
            val tOffset = t.block.size * Vars.tilesize / 2f
            val tLastTrail = t.lastRailTrail[side.reflect]
            if (side % 2 == 1) { // Top or Bottom
                val thisY = oy + dir.y * thisOffset
                var targY = ty - dir.y * tOffset
                val x1 = x - widthHalf
                val x2 = x + widthHalf
                val len = (targY - thisY).absoluteValue
                val linerShrink = time * Var.NetworkNodeRailSpeed.coerceAtMost(len)
                val shrink = (linerShrink / len).pow3InIntrp * len
                targY -= dir.y * (len - shrink)
                if (this.livingTime <= t.livingTime) {
                    lastRailTrail[side].let {
                        it.center = x
                        it.curLength = (targY - thisY).absoluteValue
                        it.totalLength = len
                    }
                    Drawf.laser(beamTR, beamEndTR, x1, thisY, x1, targY, thickness)
                    Drawf.laser(beamTR, beamEndTR, x2, thisY, x2, targY, thickness)
                } else {
                    lastRailTrail[side].let {
                        it.center = x
                        it.curLength = if (tLastTrail.curLength < tLastTrail.totalLength) 0f
                        else (targY - thisY).absoluteValue
                        it.totalLength = len
                    }
                }
            } else { // Right or Left
                val thisX = ox + dir.x * thisOffset
                var targX = tx - dir.x * tOffset
                val y1 = y - widthHalf
                val y2 = y + widthHalf
                val len = (targX - thisX).absoluteValue
                val linerShrink = time * Var.NetworkNodeRailSpeed.coerceAtMost(len)
                val shrink = (linerShrink / len).pow3InIntrp * len
                targX -= dir.x * (len - shrink)

                if (this.livingTime <= t.livingTime) {
                    lastRailTrail[side].let {
                        it.center = y
                        it.curLength = (targX - thisX).absoluteValue
                        it.totalLength = len
                    }
                    Drawf.laser(beamTR, beamEndTR, thisX, y1, targX, y1, thickness)
                    Drawf.laser(beamTR, beamEndTR, thisX, y2, targX, y2, thickness)
                } else {
                    lastRailTrail[side].let {
                        it.center = y
                        it.curLength = if (tLastTrail.curLength < tLastTrail.totalLength) 0f
                        else (targX - thisX).absoluteValue
                        it.totalLength = len
                    }
                }
            }
        }
        links.forEachUnlinkSide { side ->
            val dir = coordinates[side]
            val trail = lastRailTrail[side]
            if (trail.curLength <= 0f) return@forEachUnlinkSide
            trail.curLength = (trail.curLength - Var.NetworkNodeRailSpeed * Time.delta).coerceAtLeast(0f)
            val progress = (trail.curLength / trail.totalLength).pow3InIntrp
            if (side % 2 == 1) {// Top or Bottom
                val thisY = oy + dir.y * thisOffset
                val x = trail.center
                val targY = thisY + dir.y * trail.totalLength * progress
                val x1 = x - widthHalf
                val x2 = x + widthHalf
                Drawf.laser(beamTR, beamEndTR, x1, thisY, x1, targY, thickness)
                Drawf.laser(beamTR, beamEndTR, x2, thisY, x2, targY, thickness)
            } else { // Right or Left
                val thisX = ox + dir.x * thisOffset
                val y = trail.center
                val targX = thisX + dir.x * trail.totalLength * progress
                val y1 = y - widthHalf
                val y2 = y + widthHalf
                Drawf.laser(beamTR, beamEndTR, thisX, y1, targX, y1, thickness)
                Drawf.laser(beamTR, beamEndTR, thisX, y2, targX, y2, thickness)
            }
        }
        Draw.color()
    }
}

fun INetworkNode.drawCurrentDataInSending() {
    currentOrientedNode?.let { oriented ->
        getData(dataInSending)?.let { data ->
            drawDataInSending(data, currentOriented, sendingProgress, oriented)
        }
    }
}

fun INetworkNode.calcuDistanceTo(
    t: INetworkNode, side: Side = this.getSide(t),
): Float {
    val thisOffset = block.size * Vars.tilesize / 2f
    val tOffset = t.block.size * Vars.tilesize / 2f
    val dir = coordinates[side]
    val ox = this.building.x
    val oy = this.building.y
    val tx = t.building.x
    val ty = t.building.y
    return if (side % 2 == 1) {// Top or Bottom
        val thisY = oy + dir.y * thisOffset
        val targY = ty - dir.y * tOffset
        (targY - thisY).absoluteValue
    } else { // Right or Left
        val thisX = ox + dir.x * thisOffset
        val targX = tx - dir.x * tOffset
        (targX - thisX).absoluteValue
    }
}

fun INetworkNode.drawDataInSending(
    data: PayloadData,
    side: Side,
    progress: Progress,
    t: INetworkNode,
) {
    SD.Hologram.use(Layer.effect) {
        it.blendFormerColorOpacity *= 0.5f
        val payload = data.payload
        val thisOffset = block.size * Vars.tilesize / 2f
        val ox = this.building.x
        val oy = this.building.y
        val dir = coordinates[side]
        val tx = t.building.x
        val ty = t.building.y
        val x = (ox + tx) / 2f
        val y = (oy + ty) / 2f
        val tOffset = t.block.size * Vars.tilesize / 2f
        if (side % 2 == 1) { // Top or Bottom
            val thisY = oy + dir.y * thisOffset
            val targY = ty - dir.y * tOffset
            payload.icon().DrawSize(
                x, progress.between(thisY, targY),
                width = Var.NetworkPayloadSizeInRail, height = Var.NetworkPayloadSizeInRail,
            )
        } else { // Right or Left
            val thisX = ox + dir.x * thisOffset
            val targX = tx - dir.x * tOffset
            payload.icon().DrawSize(
                progress.between(thisX, targX), y,
                width = Var.NetworkPayloadSizeInRail, height = Var.NetworkPayloadSizeInRail,
            )
        }
    }
}
