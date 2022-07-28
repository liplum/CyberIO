package net.liplum.common.shader

import arc.graphics.g2d.Draw

/**
 * Enable the shader when the shader reference isn't null.
 * If it's null, render it in a normal way.
 * `On` means using the shader without parameter given.
 */
inline fun <T : ShaderBase> T?.on(
    zIndex: Float = Draw.z(),
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
/**
 * Enable the shader and render when the shader reference isn't null.
 * If it's null, do nothing.
 * `On` means using the shader without parameter given.
 */
inline fun <T : ShaderBase> T?.onSafe(
    zIndex: Float = Draw.z(),
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
/**
 * Enable the shader and render.
 * And provide the shader as parameter.
 * `Use` means using the shader with the shader as parameter given.
 */
inline fun <T : ShaderBase> T.use(
    zIndex: Float = Draw.z(),
    crossinline func: (T) -> Unit,
) {
    Draw.draw(zIndex) {
        Draw.shader(this)
        func(this)
        Draw.shader()
        this.reset()
    }
}
/**
 * Enable the shader when the shader reference isn't null.
 * If it's null, just render in normal way.
 * And provide the shader as parameter.
 * `Use` means using the shader with the shader as parameter given.
 */
inline fun <T : ShaderBase> T?.useSafe(
    crossinline func: (T?) -> Unit,
) {
    if (this != null) {
        Draw.draw(Draw.z()) {
            Draw.shader(this)
            func(this)
            Draw.shader()
            this.reset()
        }
    } else {
        func(null)
    }
}
/**
 * Enable the shader when [isEnabled] is true, otherwise, render it in a normal way.
 * `Use` means using the shader with the shader as parameter given.
 * @param isEnabled whether to enable the shader
 * @param func when the shader isn't enabled, a null is given.
 */
inline fun <T : ShaderBase> T.useEnabled(
    isEnabled: Boolean,
    crossinline func: (T?) -> Unit,
) {
    if (isEnabled) {
        Draw.draw(Draw.z()) {
            Draw.shader(this)
            func(this)
            Draw.shader()
            this.reset()
        }
    } else {
        func(null)
    }
}
/**
 * Enable the shader when [isEnabled] is true, otherwise, render it in a normal way.
 * `On` means using the shader without parameter given.
 * @param isEnabled whether to enable the shader
 */
inline fun <T : ShaderBase> T.onEnabled(
    isEnabled: Boolean,
    crossinline func: () -> Unit,
) {
    if (isEnabled) {
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