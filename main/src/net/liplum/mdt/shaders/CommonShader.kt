package net.liplum.mdt.shaders

import arc.Core
import arc.files.Fi
import arc.util.Time
import mindustry.Vars
import net.liplum.lib.shaders.ShaderBase

class CommonShader(
    vert: Fi, frag: Fi,
) : ShaderBase(vert, frag) {
    override fun apply() {
        setUniformf("u_time", Time.time)
        setUniformf("u_resolution",
            Core.graphics.width.toFloat(),
            Core.graphics.height
                .toFloat())
        setUniformf("u_offset",
            Core.camera.position.x,
            Core.camera.position.y
        )
        if (useEffectBuffer)
            Vars.renderer.effectBuffer.texture.bind(0)
    }
}