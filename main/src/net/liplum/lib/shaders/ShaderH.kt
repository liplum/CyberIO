package net.liplum.lib.shaders

import arc.graphics.g2d.Draw
import net.liplum.registries.CioShaders

typealias SD = CioShaders

inline fun <T : ShaderBase> T?.on(
    crossinline func: (T) -> Unit,
) {
    if (this != null) {
        Draw.draw(Draw.z()) {
            Draw.shader(this)
            func(this)
            Draw.shader()
            this.reset()
        }
    }
}

inline fun <T : ShaderBase> T?.safe(
    crossinline func: () -> Unit,
) {
    if (this != null) {
        Draw.draw(Draw.z()) {
            Draw.shader(this)
            func()
            Draw.shader()
            this.reset()
        }
    } else {
        func()
    }
}

inline fun <T : ShaderBase> T.use(
    crossinline func: (T) -> Unit,
) {
    Draw.draw(Draw.z()) {
        Draw.shader(this)
        func(this)
        Draw.shader()
        this.reset()
    }
}

inline fun <T : ShaderBase> T?.on(
    zIndex: Float,
    crossinline func: () -> Unit,
) {
    if (this != null) {
        Draw.draw(zIndex) {
            Draw.shader(this)
            func()
            Draw.shader()
            this.reset()
        }
    } else {
        func()
    }
}

inline fun <T : ShaderBase> T.use(
    zIndex: Float,
    crossinline func: (T) -> Unit,
) {
    Draw.draw(zIndex) {
        Draw.shader(this)
        func(this)
        Draw.shader()
        this.reset()
    }
}
