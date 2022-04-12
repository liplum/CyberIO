package net.liplum.registries

import arc.graphics.gl.Shader
import net.liplum.CioMod
import net.liplum.ClientOnly
import net.liplum.lib.shaders.ILoadResource
import net.liplum.lib.shaders.ProgressShader
import net.liplum.lib.shaders.TrShader
import net.liplum.shaders.SurfaceShader
import net.liplum.shaders.holo.Hologram
import net.liplum.shaders.holo.HologramOld
import net.liplum.useCompatible
import java.util.*

@ClientOnly
object CioShaders {
    lateinit var DynamicColor: TrShader
    lateinit var HologramOld: HologramOld
    lateinit var Hologram: Hologram
    lateinit var Monochrome: TrShader
    lateinit var InvertColor: TrShader
    lateinit var TvStatic: TrShader
    lateinit var Pulse: TrShader
    lateinit var InvertingColorRGB: ProgressShader
    lateinit var InvertingColorRbg2HsvInHsv: ProgressShader
    lateinit var InvertingColorRbg2HsvInRgb: ProgressShader
    lateinit var Monochromize: ProgressShader
    var Cyberion: SurfaceShader? = null
    @JvmStatic
    fun init() {
        ClientOnly {
            DynamicColor = TrShader("DynamicColor").register()

            HologramOld = HologramOld("HologramOld").register()
            Hologram = Hologram("Hologram").register()

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
            Cyberion = SurfaceShader("Cyberion").register()
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

    private var AllShaders: LinkedList<Shader> = LinkedList()
    private var AllLoadable: LinkedList<ILoadResource> = LinkedList()
    private var isInited = false
}

val String.compatible: String
    get() = if (CioMod.TestGlCompatibility || this.useCompatible)
        "$this-compatible"
    else
        this
