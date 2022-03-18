package net.liplum.shaders

import arc.graphics.g2d.Draw
import arc.graphics.gl.Shader
import net.liplum.registries.CioShaders

typealias SD = CioShaders

inline fun Shader?.on(crossinline func: () -> Unit) {
    if (this != null) {
        Draw.draw(Draw.z()) {
            Draw.shader(this)
            func()
            Draw.shader()
            if(this is IReusable){
                this.reset()
            }
        }
    } else {
        func()
    }
}

inline fun Shader.use(crossinline func: (Shader) -> Unit) {
    Draw.draw(Draw.z()) {
        Draw.shader(this)
        func(this)
        Draw.shader()
        if(this is IReusable){
            this.reset()
        }
    }
}

inline fun Shader?.on(zIndex: Float, crossinline func: () -> Unit) {
    if (this != null) {
        Draw.draw(zIndex) {
            Draw.shader(this)
            func()
            Draw.shader()
            if(this is IReusable){
                this.reset()
            }
        }
    } else {
        func()
    }
}

inline fun Shader.use(zIndex: Float, crossinline func: (Shader) -> Unit) {
    Draw.draw(zIndex) {
        Draw.shader(this)
        func(this)
        Draw.shader()
        if(this is IReusable){
            this.reset()
        }
    }
}
