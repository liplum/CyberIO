package net.liplum.render

import arc.Core
import arc.graphics.Blending
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.world.Block
import mindustry.world.blocks.heat.HeatBlock
import mindustry.world.draw.DrawBlock
import net.liplum.utils.TR

class DrawHeat(
    var suffix: String = "-glow"
) : DrawBlock() {
    lateinit var heatTR: TR
    var heatColor = Color(1f, 0.22f, 0.22f, 0.8f)
    var heatPulse = 0.3f
    var heatPulseScl: Float = 10f
    var glowMult: Float = 1.2f
    override fun draw(b: Building) = b.run {
        if (!heatTR.found()) return@run
        Draw.z(Layer.blockAdditive)
        if (this is HeatBlock) {
            val heat = heat()
            if (heat > 0f) {
                val frac = heatFrac()
                Draw.z(Layer.blockAdditive)
                Draw.blend(Blending.additive)
                Draw.color(
                    heatColor,
                    frac * (heatColor.a * (1f - heatPulse + Mathf.absin(heatPulseScl, heatPulse)))
                )
                Draw.rect(heatTR, b.x, b.y)
                Draw.blend()
                Draw.color()
            }
        }
        Draw.z(Layer.block)
    }

    override fun load(block: Block) {
        heatTR = Core.atlas.find(block.name + suffix)
    }
}