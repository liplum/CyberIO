package net.liplum.mdt.render

import arc.graphics.g2d.TextureRegion
import arc.struct.Seq
import mindustry.entities.part.*
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.draw.DrawBlock
import mindustry.world.draw.DrawMulti
import mindustry.world.draw.DrawRegion
import mindustry.world.draw.DrawTurret
import net.liplum.common.shader.ShaderBase
import net.liplum.common.shader.use

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

fun DrawTurret.wrapShader(drawPart: DrawPart, shader: () -> ShaderBase?) {
    parts.add(ShaderDrawPart(drawPart, shader))
}

inline fun <T> DrawTurret.wrapShader(
    ctor: () -> T, shader: ShaderBase?, config: T.() -> Unit,
) where T : DrawPart {
    parts.add(ShaderDrawPart(ctor().apply(config)) { shader })
}

fun DrawTurret.wrapShader(drawPart: DrawPart, shader: ShaderBase?) {
    parts.add(ShaderDrawPart(drawPart) { shader })
}

inline fun DrawTurret.shapePart(
    config: ShapePart.() -> Unit,
) {
    parts.add(ShapePart().apply(config))
}

inline fun DrawTurret.flarePart(
    config: FlarePart.() -> Unit,
) {
    parts.add(FlarePart().apply(config))
}

inline fun DrawTurret.hoverPart(
    config: HoverPart.() -> Unit,
) {
    parts.add(HoverPart().apply(config))
}

inline fun DrawTurret.haloPart(
    config: HaloPart.() -> Unit,
) {
    parts.add(HaloPart().apply(config))
}

class DrawMultiSpec {
    val all = ArrayList<DrawBlock>()
    val then = this
    infix fun add(drawer: DrawBlock) {
        all += drawer
    }

    inline fun drawTurret(
        config: DrawTurret.() -> Unit,
    ) {
        all += DrawTurret().apply(config)
    }

    inline fun drawRegion(
        suffix: String = "",
        config: DrawRegion.() -> Unit,
    ) {
        all += DrawRegion(suffix).apply(config)
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

class ShaderDrawPart(
    val inner: DrawPart,
    val shader: () -> ShaderBase?,
) : DrawPart() {
    init {
        refresh()
    }

    override fun draw(params: PartParams) {
        shader()?.use {
            inner.draw(params)
        }
    }

    override fun load(name: String) {
        inner.load(name)
    }

    override fun getOutlines(out: Seq<TextureRegion>) {
        inner.getOutlines(out)
    }

    fun refresh() {
        turretShading = inner.turretShading
        under = inner.under
        weaponIndex = inner.weaponIndex
    }
}

