package net.liplum.lib.shaders

import arc.Core
import arc.files.Fi
import arc.util.Time
import mindustry.Vars

class ProgressShader(
    vert: Fi, frag: Fi,
) : ShaderBase(vert, frag) {
    var progress = 0f
    override fun apply() {
        setUniformf("u_time", Time.time)
        setUniformf("u_progress", progress)
        setUniformf("u_resolution",
            Core.graphics.width.toFloat(),
            Core.graphics.height
                .toFloat())
        setUniformf("u_offset",
            Core.camera.position.x,
            Core.camera.position.y
        )
        Vars.renderer.effectBuffer.texture.bind(0)
    }

    override fun reset() {
        super.reset()
        progress = 0f
    }
}