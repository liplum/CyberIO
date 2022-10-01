package net.liplum.holo

import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.world.draw.DrawBlock
import net.liplum.common.shader.use
import net.liplum.holo.HoloProjector.HoloProjectorBuild
import net.liplum.registry.SD
import plumy.animation.ContextDraw.Draw

class DrawProjectingHoloUnit : DrawBlock() {
    var layer = Layer.flyingUnit + 0.1f
    override fun draw(build: Building) {
        if (build !is HoloProjectorBuild) return
        build.run {
            if (preparing <= 0f) return@run
            val type = curPlan?.unitType ?: return
            val alpha = progress * warmup
            if (alpha <= 0.01f) return@run
            SD.Hologram.use(layer) {
                it.alpha = alpha
                if (type.ColorOpacity > 0f) it.blendFormerColorOpacity = type.ColorOpacity
                if (type.HoloOpacity > 0f) it.blendHoloColorOpacity = type.HoloOpacity
                type.fullIcon.Draw(x, y)
            }
        }
    }
}