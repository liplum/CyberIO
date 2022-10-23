package net.liplum.registry

import mindustry.Vars
import mindustry.graphics.Shaders.getShaderFi
import net.liplum.R
import net.liplum.Var
import net.liplum.annotations.Only
import net.liplum.annotations.SubscribeEvent
import net.liplum.common.insertLineNumber
import net.liplum.common.shader.*
import net.liplum.event.CioInitEvent
import net.liplum.registry.CioShader.*
import net.liplum.shaders.CommonShader
import net.liplum.shaders.ProgressShader
import net.liplum.shaders.HologramShader
import net.liplum.shaders.HologramizeShader
import net.liplum.shaders.SurfaceShader
import net.liplum.shaders.VanishingShader
import net.liplum.useCompatible
import plumy.core.ClientOnly

typealias SD = CioShader

object CioShaderLoader {
    @JvmStatic
    @ClientOnly
    fun init() {
        // @formatter:off
// Dynamic
DynamicColor                = default("DynamicColor",                  ::CommonShader)
// Hologram
Hologram                    = default("Hologram",                      ::HologramShader)

Monochrome                  = default("Monochrome",                    ::CommonShader)
InvertColor                 = default("InvertColor",                   ::CommonShader)
TvStatic                    = default("TvStatic-compatible",           ::CommonShader)
Pulse                       = default("Pulse",                         ::CommonShader)
// Progressed
InvertingColorRGB           = default("InvertingColorRgb",             ::ProgressShader)
InvertingColorRbg2HsvInHsv  = default("InvertingColorRgb2HsvInHsv",    ::ProgressShader)
InvertingColorRbg2HsvInRgb  = default("InvertingColorRgb2HsvInRgb",    ::ProgressShader)
Monochromize                = default("Monochromize",                  ::ProgressShader)
// Block Surface
Cyberion                    = screen("Cyberion",                       ::SurfaceShader)
Vanishing                   = default("Vanishing",                     ::VanishingShader)
Hologramize                 = default("Hologramize",                   ::HologramizeShader)

// They can be used on the screen space.
//TestShieldScreen            = screen("TestShield",                     ::TestShieldShader)
//TestScreen                  = screen("Hologram",                       ::HologramShader)
//Glitch                       = default("Glitch",                         ::CommonShader)
        // @formatter:on
    }
    @ClientOnly
    val String.fragFileInCio: FragFi
        get() = Vars.tree.get(R.SD.GenFrag(this.removeSuffix(".frag")))
    @ClientOnly
    inline fun <T : ShaderBase> default(
        fragName: String,
        ctor: ShaderCtor<T>,
        tryCompatible: Boolean = false,
    ) = wrap("default", fragName, ctor, tryCompatible)
    @ClientOnly
    inline fun <T : ShaderBase> screen(
        fragName: String,
        ctor: ShaderCtor<T>,
        tryCompatible: Boolean = false,
    ) = wrap("screenspace", fragName, ctor, tryCompatible)
    /**
     * @param vertName use vertex shader in vanilla's tree. Only the name is needed.
     * @param fragName use fragment shader in Cyber IO's tree. Only the name is needed
     * @param ctor the constructor of shaders.
     */
    @ClientOnly
    inline fun <T : ShaderBase> wrap(
        vertName: String,
        fragName: String,
        ctor: ShaderCtor<T>,
        tryCompatible: Boolean = false,
    ): T {
        val fragFileName = if (tryCompatible) fragName.compatible else fragName
        val fragFile = fragFileName.fragFileInCio
        val vertFileName = "${vertName.removeSuffix(".vert")}.vert"
        val vertFile = getShaderFi(vertFileName)
        try {
            val shader = ctor(vertFile, fragFile)
            return shader.register()
        } catch (e: Exception) {
            var fragment = "Re-preprocess fragment error!"
            try {
                fragment = preprocessFragment(fragFile).insertLineNumber { "[$it]" }
            } catch (_: Exception) {
            }
            throw ShaderCompileException(
                "Can't compile shaders $vertFileName and $fragFileName.\n$fragment\n${e.message}", e
            )
        }
    }
    @JvmStatic
    @ClientOnly
    @SubscribeEvent(CioInitEvent::class, Only.client)
    fun loadResource() {
        for (loadable in AllShaders) {
            loadable.loadResource()
        }
    }
    @JvmStatic
    @ClientOnly
    fun dispose() {
        for (shader in AllShaders) {
            shader.dispose()
        }
    }
    @ClientOnly
    fun <T> T.register(): T where T : ShaderBase {
        AllShaders.add(this)
        return this
    }

    private var AllShaders: HashSet<ShaderBase> = HashSet()
}
typealias ShaderCtor<T> = (FragFi, VertFi) -> T

val String.compatible: String
    get() = if (Var.TestGlCompatibility || this.useCompatible)
        "$this-compatible"
    else
        this
