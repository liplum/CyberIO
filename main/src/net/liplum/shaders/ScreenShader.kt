package net.liplum.shaders

import arc.Core
import arc.files.Fi
import arc.scene.ui.layout.Scl
import arc.util.Time
import mindustry.graphics.Shaders.getShaderFi
import net.liplum.common.shader.ShaderBase

class TestShieldShader : ShaderBase {
    constructor(vert: Fi, frag: Fi) : super(vert, frag)
    constructor(frag: Fi) : super(getShaderFi("screenspace.vert"), frag)

    override fun apply() {
        setUniformf("u_dp", Scl.scl(1f))
        setUniformf("u_time", Time.time / Scl.scl(1f))
        setUniformf(
            "u_offset",
            Core.camera.position.x - Core.camera.width / 2,
            Core.camera.position.y - Core.camera.height / 2
        )
        setUniformf("u_texsize", Core.camera.width, Core.camera.height)
        setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height)
    }
}
