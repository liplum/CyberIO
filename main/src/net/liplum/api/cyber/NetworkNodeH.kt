@file:JvmName("DataNetworkH")

package net.liplum.api.cyber

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.geom.Geometry
import arc.scene.ui.Image
import arc.scene.ui.Label
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.Table
import mindustry.Vars
import mindustry.Vars.world
import mindustry.gen.Iconc
import mindustry.gen.Tex
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.ui.Styles
import mindustry.world.blocks.payloads.Payload
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.api.cyber.SideLinks.Companion.coordinates
import net.liplum.lib.utils.DrawLayer
import net.liplum.mdt.advanced.Inspector.isPlacing
import net.liplum.mdt.advanced.Inspector.isSelected
import net.liplum.mdt.render.G
import net.liplum.mdt.render.Text
import net.liplum.mdt.render.smoothPlacing
import net.liplum.mdt.render.smoothSelect
import net.liplum.mdt.utils.TEAny
import net.liplum.mdt.utils.TileXY
import net.liplum.mdt.utils.build
import net.liplum.mdt.utils.worldXY

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
            if (b is INetworkNode) {
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
    for (i in 0..3) {
        var maxLen = (range + size / 2f).worldXY
        var limit = -1f
        var dest: INetworkNode? = null
        val dir = Geometry.d4[i]
        val dx = dir.x
        val dy = dir.y
        val offset = size / 2
        for (j in 1 + offset..range + offset) {
            val other = world.build(tileX + j * dir.x, tileY + j * dir.y)
            if (other != null && other.team == team && other is INetworkNode) {
                limit = j.worldXY
                dest = other
                break
            }
        }
        maxLen *= smoothSelect(expendSelectingLineTime)
        if (limit > 0f) maxLen = maxLen.coerceAtMost(limit)
        val blockOffset = (size / 2f + 2).worldXY
        val x1 = x + dx * blockOffset
        val y1 = y + dy * blockOffset
        val x2 = x + dx * maxLen
        val y2 = y + dy * maxLen
        DebugOnly {
            for (side in RIGHT..BOTTOM) {
                Text.drawTextEasy(
                    "${maxLen.toInt()}",
                    x + size * 2 * dir.x,
                    y + size * 2 * dir.y,
                    R.C.GreenSafe
                )
            }
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
}

fun INetworkBlock.drawPlaceCardinalDirections(
    x: TileXY, y: TileXY
) = block.run {
    // Draw a cross
    if (!this.isPlacing()) return
    val team = Vars.player.team()
    val range = tileLinkRange
    for (i in 0..3) {
        var maxLen = (range + size / 2f).worldXY
        var limit = -1f
        var dest: INetworkNode? = null
        val dir = Geometry.d4[i]
        val dx = dir.x
        val dy = dir.y
        val offset = size / 2
        for (j in 1 + offset..range + offset) {
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
        val blockOffset = (size / 2f + 2).worldXY
        val x1 = worldX + dx * blockOffset
        val y1 = worldY + dy * blockOffset
        val x2 = worldX + dx * maxLen
        val y2 = worldY + dy * maxLen
        DebugOnly {
            for (side in RIGHT..BOTTOM) {
                Text.drawTextEasy(
                    "${maxLen.toInt()}",
                    worldX + size * 2 * dir.x,
                    worldY + size * 2 * dir.y,
                    R.C.GreenSafe
                )
            }
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
}
@DebugOnly
fun INetworkNode.drawLinkInfo() = building.run {
    DrawLayer {
        Draw.z(Layer.overlayUI)
        forEachEnabledSide { side ->
            val link = links[side]
            val linkB = link.build
            if (linkB != null)
                G.dashLineBetweenTwoBlocksBreath(this.tile, linkB.tile)
        }

        Text.drawTextEasy("${network.id}", x, y + 5f, R.C.RedAlert)
        Text.drawTextEasy("${building.id}", x, y - 5f, R.C.Holo)
        for (side in RIGHT..BOTTOM) {
            val size = block.size.worldXY / 2f
            val dir = coordinates[side]
            val pos = links[side]
            val text = pos.TEAny<INetworkNode>()?.let { "${it.tile.x},${it.tile.y}" } ?: "-1"
            Text.drawTextEasy(
                text,
                x + size * dir.x, y + size * dir.y,
                R.C.GreenSafe
            )
        }
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

fun INetworkNode.buildNetworkDataList(table: Table) {
    table.add(ScrollPane(Table(Tex.wavepane).apply {
        network.forEachDataIndexed { i, node, payload ->
            add(Table(Tex.button).apply {
                buildPayloadDataInfo(node, payload)
            }).margin(5f).grow().size(Vars.iconXLarge * 2.5f)
            if ((i + 1) % 4 == 0) row()
        }
    }, Styles.defaultPane))
}

fun Table.buildPayloadDataInfo(node: INetworkNode, data: Payload) {
    add(Image(data.icon())).size(Vars.iconXLarge * 1.5f).row()
    val tile = node.tile
    add(Label { "${tile.x},${tile.y}" })
}