package net.liplum.render

import arc.Core
import arc.graphics.Blending
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.util.Tmp
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.Block
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild
import mindustry.world.blocks.heat.HeatBlock
import mindustry.world.draw.DrawBlock
import net.liplum.utils.draw
import plumy.core.assets.EmptyTR
import plumy.core.assets.TR
import plumy.dsl.drawX
import plumy.dsl.drawY

data class HeatMeta(
    var heatColor: Color = Color(1f, 0.22f, 0.22f, 0.8f),
    var heatPulse: Float = 0.3f,
    var heatPulseScl: Float = 10f,
    var glowMultiplier: Float = 1.2f,
)

fun HeatMeta.drawHeat(b: Building, tr: TR, heatFrac: Float) {
    if (heatFrac > 0f) {
        Draw.z(Layer.blockAdditive)
        Draw.blend(Blending.additive)
        Draw.color(
            heatColor,
            heatFrac * (heatColor.a * (1f - heatPulse + Mathf.absin(heatPulseScl, heatPulse)))
        )
        Draw.rect(tr, b.x, b.y)
        Draw.blend()
        Draw.color()
        Draw.z(Layer.block)
    }
}

fun HeatMeta.drawHeatAt(x: Float, y: Float, tr: TR, heatFrac: Float) {
    if (heatFrac > 0f) {
        Draw.z(Layer.blockAdditive)
        Draw.blend(Blending.additive)
        Draw.color(
            heatColor,
            heatFrac * (heatColor.a * (1f - heatPulse + Mathf.absin(heatPulseScl, heatPulse)))
        )
        Draw.rect(tr, x, y)
        Draw.blend()
        Draw.color()
        Draw.z(Layer.block)
    }
}

inline fun HeatMeta.drawHeat(heatFrac: Float, draw: () -> Unit) {
    if (heatFrac > 0f) {
        Draw.blend(Blending.additive)
        Draw.color(
            heatColor,
            heatFrac * (heatColor.a * (1f - heatPulse + Mathf.absin(heatPulseScl, heatPulse)))
        )
        draw()
        Draw.blend()
        Draw.color()
    }
}

fun HeatMeta.drawHeat(b: Building, tr: TR) {
    if (b is HeatBlock) {
        val heatFrac = b.heatFrac()
        if (heatFrac > 0f) {
            Draw.z(Layer.blockAdditive)
            Draw.blend(Blending.additive)
            Draw.color(
                heatColor,
                heatFrac * (heatColor.a * (1f - heatPulse + Mathf.absin(heatPulseScl, heatPulse)))
            )
            Draw.rect(tr, b.x, b.y)
            Draw.blend()
            Draw.color()
            Draw.z(Layer.block)
        }
    }
}

class DrawTurretHeat<T : TurretBuild>(
    var suffix: String = "-heat",
    var heatProgress: T.() -> Float = { heat },
) : DrawBlock() {
    var heat = HeatMeta()
    var heatTR = EmptyTR
    var turretHeatLayer = Layer.turretHeat
    var heatLayerOffset = 1f
    var turretShading = false
    override fun load(block: Block) = block.run {
        heatTR = Core.atlas.find("${block.name}$suffix")
    }
    @Suppress("UNCHECKED_CAST")
    override fun draw(build: Building) = (build as T).run {
        if (this@DrawTurretHeat.heatTR.found()) {
            Drawf.additive(
                this@DrawTurretHeat.heatTR,
                this@DrawTurretHeat.heat.heatColor.write(Tmp.c1).a(heatProgress() * this@DrawTurretHeat.heat.heatColor.a),
                drawX,
                drawY,
                rotation.draw,
                if (turretShading) turretHeatLayer else Draw.z() + heatLayerOffset
            )
        }
    }
}

class DrawHeatBlock(
    var suffix: String = "-heat",
) : DrawBlock() {
    lateinit var heatTR: TR
    var heatColor = Color(1f, 0.22f, 0.22f, 0.8f)
    var heatPulse = 0.3f
    var heatPulseScl: Float = 10f
    var glowMult: Float = 1.2f
    override fun draw(b: Building) = b.run {
        if (!heatTR.found()) return@run
        if (this is HeatBlock) {
            val frac = heatFrac()
            if (frac > 0f) {
                Draw.z(Layer.blockAdditive)
                Draw.z(Layer.blockAdditive)
                Draw.blend(Blending.additive)
                Draw.color(
                    heatColor,
                    frac * (heatColor.a * (1f - heatPulse + Mathf.absin(heatPulseScl, heatPulse)))
                )
                Draw.rect(heatTR, b.x, b.y)
                Draw.blend()
                Draw.color()
                Draw.z(Layer.block)
            }
        }
    }

    override fun load(block: Block) {
        heatTR = Core.atlas.find(block.name + suffix)
    }
}