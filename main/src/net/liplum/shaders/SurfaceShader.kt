package net.liplum.shaders

import arc.Core
import arc.files.Fi
import arc.func.Cons
import arc.graphics.Texture
import arc.util.Time
import mindustry.Vars
import mindustry.graphics.Shaders.getShaderFi
import net.liplum.common.shader.ShaderBase

class SurfaceShader(
    vert: Fi, frag: Fi,
) : ShaderBase(vert, frag) {
    constructor(frag: Fi) : this(getShaderFi("screenspace.vert"), frag)

    var noise: Texture? = null
    override fun loadResource() {
        val ad = Core.assets.load("sprites/noise.png", Texture::class.java)
        ad.loaded = Cons { t: Texture ->
            t.setFilter(Texture.TextureFilter.linear)
            t.setWrap(Texture.TextureWrap.repeat)
        }
    }

    override fun apply() {
        setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2)
        setUniformf("u_resolution", Core.camera.width, Core.camera.height)
        setUniformf("u_time", Time.time)
        if (hasUniform("u_noise")) {
            if (noise == null) {
                noise = Core.assets.get("sprites/noise.png", Texture::class.java)
            }
            noise!!.bind(1)
            Vars.renderer.effectBuffer.texture.bind(0)
            setUniformi("u_noise", 1)
        }
    }
}
