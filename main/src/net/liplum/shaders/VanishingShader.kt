package net.liplum.shaders

import arc.files.Fi
import arc.graphics.Color
import arc.util.Time
import net.liplum.S
import net.liplum.lib.TR
import net.liplum.lib.shaders.ShaderBase

class VanishingShader(
    vert: Fi,
    frag: Fi
) : ShaderBase(vert, frag) {
    var progress = 0f
    var offset = 1f
    var scanlineColor = Color(S.Hologram)
    var region = TR()
    var isTopDown = true

    override fun apply() {
        setUniformf("u_time", Time.time)
        setUniformf("u_offset", offset)
        setUniformf("u_scanline_color", scanlineColor)
        setUniformf("u_progress", progress)
        setUniformf("u_uv", region.u, region.v)
        setUniformf("u_uv2", region.u2, region.v2)
        setUniformi("u_topDown", if (isTopDown) 1 else 0)
        setUniformf(
            "u_size",
            region.texture.width.toFloat(),
            region.texture.height.toFloat()
        )
    }

    override fun reset() {
        progress = 0f
        offset = 1f
        isTopDown = true
        scanlineColor.set(S.Hologram)
    }
}