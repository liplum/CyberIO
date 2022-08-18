package net.liplum.mdt.render

import arc.graphics.g2d.TextureRegion
import arc.struct.Seq
import mindustry.entities.part.*
import mindustry.entities.part.DrawPart.PartProgress
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.draw.DrawBlock
import mindustry.world.draw.DrawMulti
import mindustry.world.draw.DrawRegion
import mindustry.world.draw.DrawTurret
import net.liplum.common.shader.ShaderBase
import net.liplum.common.shader.use

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

