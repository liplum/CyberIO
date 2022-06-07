@file:JvmName("DataNetworkH")

package net.liplum.api.cyber

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.geom.Geometry
import arc.scene.event.Touchable
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.Image
import arc.scene.ui.Label
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.Stack
import arc.scene.ui.layout.Table
import arc.scene.utils.Elem
import mindustry.Vars
import mindustry.Vars.world
import mindustry.gen.Iconc
import mindustry.gen.Tex
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.ui.Styles
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.S
import net.liplum.Var
import net.liplum.api.cyber.SideLinks.Companion.coordinates
import net.liplum.data.EmptyDataID
import net.liplum.data.PayloadData
import net.liplum.lib.TR
import net.liplum.lib.math.smooth
import net.liplum.lib.utils.DrawLayer
import net.liplum.mdt.advanced.Inspector.isPlacing
import net.liplum.mdt.advanced.Inspector.isSelected
import net.liplum.mdt.render.G
import net.liplum.mdt.render.Text
import net.liplum.mdt.render.smoothPlacing
import net.liplum.mdt.render.smoothSelect
import net.liplum.mdt.utils.*
import kotlin.math.absoluteValue

val destinations = arrayOfNulls<INetworkNode>(4)
/**
 * Update the link in four cardinal directions.
 * This function may add link or remove link.
 * # Add or Remove
 * `Add link` will affect two nodes.
 * `Remove link` will only affect who calls this function.
 */
fun INetworkNode.updateCardinalDirections() = building.run {
    val offset = block.size / 2
    val tileX = tile.x.toInt()
    val tileY = tile.y.toInt()
    val range = tileLinkRange
    forEachEnabledSide { side ->
        val pre = links[side]
        val dir = coordinates[side]
        destinations[side] = null
        for (j in 1 + offset..range + offset) { // expending
            val b = world.build(tileX + j * dir.x, tileY + j * dir.y)
            if (b != null && b.team == team && b is INetworkNode) {
                destinations[side] = b
                break
            }
        }
        val dest = destinations[side]
        if (dest != null) { // reach a node
            if (dest.building.pos() != pre) {// if not the previous one
                if (canLink(side, dest)) {
                    link(side, dest)
                }// else : reached is invalid -> do nothing
            }// is previous one -> do nothing
        } else {
            // doesn't reach any node -> clear current side
            clearSide(side)
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
            if (other != null && other.team == team && other is INetworkNode) {
                limit = j.worldXY
                dest = other
                break
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
fun INetworkNode.drawLinkInfo() = building.run {
    DrawLayer {
        Draw.z(Layer.overlayUI)
        if (sendingProgress > 0f) {
            val width = 25f
            Fill.rect(x, y, width, 5f)
            Draw.color(S.Hologram)
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
            val text = pos.TEAny<INetworkNode>()?.let { "${it.tile.x},${it.tile.y}" } ?: "-1"
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
    G.circleBreath(toCenterWorldXY(x), toCenterWorldXY(y), linkRange, alpha = alpha)
}

fun INetworkNode.buildNetworkDataList(table: Table) {
    table.add(ScrollPane(Table(Tex.wavepane).apply {
        network.forEachDataIndexed { i, node, data ->
            add(Table(Tex.button).apply {
                buildPayloadDataInfo(node, data)
            }).margin(5f).grow().size(Vars.iconXLarge * 2.5f)
            if ((i + 1) % 4 == 0) row()
        }
    }, Styles.defaultPane))
}

fun Table.buildPayloadDataInfo(node: INetworkNode, data: PayloadData) {
    add(Stack(
        Image(data.payload.icon()),
        Label("${data.id}"),
    )
    ).size(Vars.iconXLarge * 1.5f).row()
    val tile = node.tile
    add(Label { "${tile.x},${tile.y}" })
}

fun INetworkNode.buildNetworkDataListSelector(table: Table) {
    table.add(ScrollPane(Table(Tex.wavepane).apply {
        network.forEachDataIndexed { i, node, data ->
            add(Table(Tex.button).apply {
                buildPayloadDataInfoSelectorItem(this@buildNetworkDataListSelector, node, data)
            }).margin(5f).grow().size(Vars.iconXLarge * 2.5f)
            if ((i + 1) % 4 == 0) row()
        }
    }, Styles.defaultPane))
}

fun Table.buildPayloadDataInfoSelectorItem(cur: INetworkNode, node: INetworkNode, data: PayloadData) {
    add(
        Stack(
            Elem.newImageButton(TextureRegionDrawable(data.payload.icon())) {
                cur.request = data.id
            },
            Label("${data.id}").apply {
                touchable = Touchable.disabled
            },
        )
    ).size(Vars.iconXLarge * 1.5f).row()
    val tile = node.tile
    add(Label { "${tile.x},${tile.y}" })
}

fun INetworkNode.drawRail(beamTR: TR, beamEndTR: TR) {
    val thisOffset = block.size * Vars.tilesize / 2f
    val ox = this.building.x
    val oy = this.building.y
    val widthHalf = Var.NetworkNodeChannelWidth / 2f
    val thickness = 0.2f
    Draw.color(S.Hologram)
    links.forEachNodeWithSide { side, t ->
        val time = linkingTime[side]
        val dir = coordinates[side]
        val tx = t.building.x
        val ty = t.building.y
        val x = (ox + tx) / 2f
        val y = (oy + ty) / 2f
        val tOffset = t.block.size * Vars.tilesize / 2f
        if (side % 2 == 1) {
            val thisY = oy + dir.y * thisOffset
            var targY = ty - dir.y * tOffset
            val x1 = x - widthHalf
            val x2 = x + widthHalf
            val len = (targY - thisY).absoluteValue
            val linerShrink = time * Var.NetworkNodeRailSpeed.coerceAtMost(len)
            val shrink = (linerShrink / len).smooth * len
            targY -= dir.y * (len - shrink)
            Drawf.laser(beamTR, beamEndTR, x1, thisY, x1, targY, thickness)
            Drawf.laser(beamTR, beamEndTR, x2, thisY, x2, targY, thickness)
        } else {
            val thisX = ox + dir.x * thisOffset
            var targX = tx - dir.x * tOffset
            val y1 = y - widthHalf
            val y2 = y + widthHalf
            val len = (targX - thisX).absoluteValue
            val linerShrink = time * Var.NetworkNodeRailSpeed.coerceAtMost(len)
            val shrink = (linerShrink / len).smooth * len
            targX -= dir.x * (len - shrink)
            Drawf.laser(beamTR, beamEndTR, thisX, y1, targX, y1, thickness)
            Drawf.laser(beamTR, beamEndTR, thisX, y2, targX, y2, thickness)
        }
    }
    Draw.color()
}