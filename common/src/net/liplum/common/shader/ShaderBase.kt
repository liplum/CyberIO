package net.liplum.common.shader

import arc.files.Fi
import arc.graphics.gl.Shader

open class ShaderBase(
    vert: Fi, frag: Fi,
) : Shader(vert, frag) {
    @JvmField var useEffectBuffer = true
    open fun reset() {
        useEffectBuffer = true
    }

    open fun loadResource() {
    }
}