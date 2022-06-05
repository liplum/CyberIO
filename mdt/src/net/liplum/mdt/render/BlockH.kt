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
import net.liplum.mdt.utils.toCenterWorldXY
import net.liplum.mdt.utils.worldXY

@ClientOnly
inline fun Block.drawSurroundingRect(
    tileX: TileXY,
    tileY: TileXY,
    extension: TileXYf,
    color: Color,
    crossinline filter: (Building) -> Boolean
) {
    val worldX = tileX.worldXY
    val worldY = tileY.worldXY
    val rect = Tmp.r1
    rect.setCentered(
        worldX + offset, worldY + offset,
        (size + extension).worldXY
    )
    Drawf.dashRect(color, rect)
    Vars.indexer.eachBlock(Vars.player.team(), rect, { filter(it) }) {
        Drawf.selected(
            it, Tmp.c1.set(color).a(Mathf.absin(4f, 1f))
        )
    }
}
@ClientOnly
fun Block.drawEffectCirclePlace(
    x: TileXY, y: TileXY,
    circleColor: Color, range: Float,
    filter: Building.() -> Boolean = { true },
    stroke: Float = 1f,
    func: Building. () -> Unit,
) {
    G.dashCircleBreath(
        toCenterWorldXY(x), toCenterWorldXY(y), range,
        circleColor, stroke = stroke
    )
    Vars.indexer.eachBlock(
        Vars.player.team(), toCenterWorldXY(x), toCenterWorldXY(y), range,
        filter, func
    )
}