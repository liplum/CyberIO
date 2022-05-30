@file:JvmName("BlockH")

package net.liplum.mdt.render

import arc.graphics.Color
import arc.math.Mathf
import arc.util.Tmp
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.world.Block
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.TileXY
import net.liplum.mdt.utils.TileXYf
import net.liplum.mdt.utils.worldXY

/**
 * @param progress only for visual effects
 */
@ClientOnly
@JvmOverloads
inline fun Block.drawSurroundingRect(
    tileX: TileXY,
    tileY: TileXY,
    extension: TileXYf,
    color: Color,
    progress: Float = 1f,
    crossinline filter: (Building) -> Boolean
) {
    val worldX = tileX.worldXY
    val worldY = tileY.worldXY
    val rect = Tmp.r1
    rect.setCentered(
        worldX + offset, worldY + offset,
        (size + extension).worldXY
    )
    Vars.indexer.eachBlock(Vars.player.team(), rect, { filter(it) }) {
        Drawf.selected(
            it, Tmp.c1.set(color).a(Mathf.absin(4f, 1f))
        )
    }
    rect.setCentered(
        worldX + offset, worldY + offset,
        (size + extension * progress).worldXY
    )
    Drawf.dashRect(color, rect)
}