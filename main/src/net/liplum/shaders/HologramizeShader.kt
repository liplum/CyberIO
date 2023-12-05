package net.liplum.shaders

import arc.files.Fi
import arc.graphics.Color
import arc.util.Time
import net.liplum.Var
import plumy.core.assets.TR
import net.liplum.common.shader.ShaderBase

class HologramizeShader(
    vert: Fi,
    frag: Fi,
) : ShaderBase(vert, frag) {
    var progress = 0f
    var scanlineWidthPct = 0.1f // [0f,1f] <= 0.2 is better
    var region = TR()
    var isTopDown = true
    var opacityNoise = 0.2f
    var blendHoloColorOpacity = DefaultBlendHoloColorOpacity
    var blendFormerColorOpacity = DefaultBlendFormerColorOpacity
    var flickering = DefaultFlickering
    var holoColor = Color(Var.Hologram)
    override fun apply() {
        setUniformf("u_time", Time.time)
        setUniformf("u_scanline_width", scanlineWidthPct)
        setUniformf("u_progress", progress)
        setUniformf("u_uv", region.u, region.v)
        setUniformf("u_uv2", region.u2, region.v2)
        setUniformi("u_topDown", if (isTopDown) 1 else 0)
        setUniformf("u_holo_color", holoColor)
        setUniformf("u_opacityNoise", opacityNoise)
        setUniformf("u_flickering", flickering)
        setUniformf("u_blendHoloColorOpacity", blendHoloColorOpacity)
        setUniformf("u_blendFormerColorOpacity", blendFormerColorOpacity)
        setUniformf(
            "u_size",
            region.texture.width.toFloat(),
            region.texture.height.toFloat()
        )
    }

    override fun reset() {
        progress = 0f
        scanlineWidthPct = 0.1f
        opacityNoise = 0.2f
        flickering = DefaultFlickering
        blendHoloColorOpacity = DefaultBlendHoloColorOpacity
        blendFormerColorOpacity = DefaultBlendFormerColorOpacity
        holoColor.set(Var.Hologram)
        isTopDown = true
    }

    companion object {
        const val DefaultBlendHoloColorOpacity = 0.8f
        const val DefaultBlendFormerColorOpacity = 0.6f
        const val DefaultFlickering = 0.03f
    }
}