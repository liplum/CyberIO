@file:JvmName("DataNetworkH")

package net.liplum.api.cyber

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.geom.Geometry
import mindustry.Vars
import mindustry.Vars.world
import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.lib.segmentLines
import net.liplum.lib.utils.forEach
import net.liplum.mdt.advanced.Inspector.isPlacing
import net.liplum.mdt.render.G
import net.liplum.mdt.render.Text
import net.liplum.mdt.render.smoothPlacing
import net.liplum.mdt.utils.TileXY
import net.liplum.mdt.utils.build
import net.liplum.mdt.utils.worldXY

val destinations = arrayOfNulls<INetworkNode>(4)
fun INetworkNode.updateCardinalDirections() = building.run {
    if (!canLinkMore) return@run
    val offset = block.size / 2
    val tileX = tile.x.toInt()
    val tileY = tile.y.toInt()
    val range = tileLinkRange
    for (i in 0..3) {
        val dir = Geometry.d4[i]
        destinations[i] = null
        for (j in 1 + offset..range + offset) {
            val b = world.build(tileX + j * dir.x, tileY + j * dir.y)
            if (b is INetworkNode) {
                if (canLink(b))
                    destinations[i] = b
                break
            }
        }
        val dest = destinations[i]
        if (dest != null && !isLinkedWith(dest) && canLink(dest)) {
            link(dest)
        }
    }
}
/**
 * Get the reflected side index in [Geometry.d4]
 */
fun sideReverse(original: Int): Int =
    (original + 2) % 4

fun ISideNetworkNode.updateCardinalDirections() = building.run {
    if (!canLinkMore) return@run
    val offset = block.size / 2
    val tileX = tile.x.toInt()
    val tileY = tile.y.toInt()
    val range = tileLinkRange
    var found = false // whether it finds a node that could be linked with
    for (i in 0..3) {
        val previous = sideLinks[i]
        val dir = Geometry.d4[i]
        destinations[i] = null
        for (j in 1 + offset..range + offset) {
            when (val b = world.build(tileX + j * dir.x, tileY + j * dir.y)) {
                is INetworkNode -> {
                    // It can't be the previous one or out of range
                    if (b.building.pos() != previous && canLink(b))
                        destinations[i] = b
                    found = true
                    break
                }
                is ISideNetworkNode -> {
                    // It can't be the previous one or out of range
                    if (b.building.pos() != previous && canSideLink(b, i))
                        destinations[i] = b
                    found = true
                    break
                }
            }
        }
        if (found) {// if found, try to link with it
            val dest = destinations[i]
            // Only link a new node. `dest == null` mean can't link it, or it's old node
            if (dest != null) {
                linkSide(i, dest)
            }
        } else {// if not found, unlink this side
            unlinkSide(i)
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
        var maxLen = range + size / 2f
        var limit = -1f
        var dest: Building? = null
        val dir = Geometry.d4[i]
        val dx = dir.x
        val dy = dir.y
        val offset = size / 2
        for (j in 1 + offset..range + offset) {
            val other = world.build(x + j * dir.x, y + j * dir.y)
            if (other != null && other.team == team && other is INetworkNode) {
                limit = j.toFloat()
                dest = other
                break
            }
        }
        maxLen *= smoothPlacing(expendPlacingLineTime)
        if (limit > 0f) maxLen = maxLen.coerceAtMost(limit)
        val worldX = x * Vars.tilesize
        val worldY = y * Vars.tilesize
        val blockOffset = Vars.tilesize * size / 2f + 2
        val x1 = worldX + dx * blockOffset
        val y1 = worldY + dy * blockOffset
        val x2 = worldX + dx * maxLen * Vars.tilesize
        val y2 = worldY + dy * maxLen * Vars.tilesize
        G.drawLineBreath(Pal.placing, x1, y1, x2, y2, stroke = 2f)

        if (dest != null) {
            G.drawWrappedSquareBreath(dest)
        }
    }
}
@DebugOnly
fun INetworkNode.drawNetworkInfo() = building.run {
    val originalZ = Draw.z()
    Draw.z(Layer.overlayUI)
    for (i in 0 until dataMod.links.size) {
        val link = dataMod.links[i]
        val linkB = link.build
        if (linkB != null)
            G.drawDashLineBetweenTwoBlocksBreath(this.tile, linkB.tile)
    }
    Text.drawTextEasy("${dataMod.network.id}", x, y + block.size.worldXY / 2f, R.C.RedAlert)
    val tmp = ArrayList<String>()
    dataMod.links.forEach {i ->
        i.build?.let {
            tmp.add("${it.tileX()},${it.tileY()}")
        }
    }
    Text.drawTextEasy(
        "${tmp}\n${networkGraph.nodes}".segmentLines(50),
        x, y, Color.white
    )
    if (this is ISideNetworkNode) {
        val size = block.size.worldXY / 2f
        for (i in 0..3) {
            val dir = Geometry.d4[i]
            Text.drawTextEasy(
                "${sideLinks[i]}",
                x + size * dir.x, y + size * dir.y,
                R.C.GreenSafe
            )
        }
    }
    Draw.z(originalZ)
}