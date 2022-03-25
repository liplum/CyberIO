package net.liplum.registries

import arc.graphics.gl.Shader
import net.liplum.CioMod
import net.liplum.ClientOnly
import net.liplum.lib.shaders.ILoadResource
import net.liplum.lib.shaders.ProgressShader
import net.liplum.lib.shaders.TrShader
import net.liplum.shaders.holo.Hologram
import net.liplum.shaders.holo.Hologram2
import net.liplum.useCompatible
import java.util.*

object CioShaders {
    @ClientOnly lateinit var DynamicColor: TrShader
    @ClientOnly lateinit var Hologram: Hologram
    @ClientOnly lateinit var Hologram2: Hologram2
    @ClientOnly lateinit var Monochrome: TrShader
    @ClientOnly lateinit var InvertColor: TrShader
    @ClientOnly lateinit var TvStatic: TrShader
    @ClientOnly lateinit var Pulse: TrShader
    @ClientOnly lateinit var InvertingColorRGB: ProgressShader
    @ClientOnly lateinit var InvertingColorRbg2HsvInHsv: ProgressShader
    @ClientOnly lateinit var InvertingColorRbg2HsvInRgb: ProgressShader
    @ClientOnly lateinit var Monochromize: ProgressShader
    @ClientOnly
    private var AllShaders: LinkedList<Shader> = LinkedList()
    private var AllLoadable: LinkedList<ILoadResource> = LinkedList()
    private var isInited = false
    @JvmStatic
    fun init() {
        ClientOnly {
            DynamicColor = TrShader("DynamicColor").register()

            Hologram = Hologram("Hologram").register()
            Hologram2 = Hologram2("Hologram2").register()

            Monochrome = TrShader("Monochrome").register()
            InvertColor = TrShader("InvertColor").register()
            TvStatic = TrShader("TvStatic".compatible).register()
            Pulse = TrShader("Pulse").register()

            InvertingColorRGB = ProgressShader("InvertingColorRgb".compatible)
                .register()
            InvertingColorRbg2HsvInHsv = ProgressShader("InvertingColorRgb2HsvInHsv")
                .register()
            InvertingColorRbg2HsvInRgb = ProgressShader("InvertingColorRgb2HsvInRgb")
                .register()

            Monochromize = ProgressShader("Monochromize".compatible).register()

            isInited = true
        }
    }
    @JvmStatic
    fun loadResource() {
        ClientOnly {
            if (isInited) {
                for (loadable in AllLoadable) {
                    loadable.loadResource()
                }
            }
        }
    }
    @ClientOnly
    @JvmStatic
    fun dispose() {
        ClientOnly {
            if (isInited) {
                for (shader in AllShaders) {
                    shader.dispose()
                }
            }
        }
    }
    @ClientOnly
    fun <T> T.register(): T where T : Shader {
        AllShaders.add(this)
        if (this is ILoadResource) {
            AllLoadable.add(this)
        }
        return this
    }
}

val String.compatible: String
    get() = if (CioMod.TestGlCompatibility || this.useCompatible)
        "$this-compatible"
    else
        this
