package net.liplum.mdt.render

import mindustry.entities.part.RegionPart
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.draw.DrawBlock
import mindustry.world.draw.DrawMulti
import mindustry.world.draw.DrawRegion
import mindustry.world.draw.DrawTurret

inline fun Turret.drawTurret(
    config: DrawTurret.() -> Unit,
) {
    this.drawer = DrawTurret().apply(config)
}

inline fun DrawTurret(
    config: DrawTurret.() -> Unit,
) = DrawTurret().apply(config)

inline fun DrawTurret.regionPart(
    suffix: String? = null,
    config: RegionPart.() -> Unit,
) {
    parts.add(RegionPart(suffix).apply(config))
}

class DrawMultiSpec {
    val all = ArrayList<DrawBlock>()
    val add = this
    infix fun a(drawer: DrawBlock) {
        all.add(drawer)
    }

    inline fun drawTurret(
        config: DrawTurret.() -> Unit,
    ) {
        all.add(DrawTurret().apply(config))
    }

    inline fun drawRegion(
        suffix: String = "",
        config: DrawRegion.() -> Unit,
    ) {
        all.add(DrawRegion(suffix).apply(config))
    }
}

inline fun DrawMulti(
    config: DrawMultiSpec.() -> Unit,
): DrawMulti = DrawMulti(*DrawMultiSpec().apply(config).all.toTypedArray())

inline fun Turret.drawMulti(
    config: DrawMultiSpec.() -> Unit,
) {
    drawer = DrawMulti(*DrawMultiSpec().apply(config).all.toTypedArray())
}