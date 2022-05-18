package net.liplum.registries

import arc.files.Fi
import mindustry.Vars
import mindustry.graphics.Shaders.getShaderFi
import net.liplum.CioMod
import net.liplum.mdt.ClientOnly
import net.liplum.R
import net.liplum.lib.shaders.*
import net.liplum.mdt.shaders.CommonShader
import net.liplum.mdt.shaders.ProgressShader
import net.liplum.registries.CioShaders.*
import net.liplum.shaders.SurfaceShader
import net.liplum.shaders.holo.HologramOldShader
import net.liplum.shaders.holo.HologramShader
import net.liplum.useCompatible

@ClientOnly
object CioShaderLoader {
    @JvmStatic
    @ClientOnly
    fun init() {
        // @formatter:off
// Dynamic
DynamicColor                = default("DynamicColor",                  ::CommonShader)
// Hologram
HologramOld                 = default("HologramOld",                   ::HologramOldShader)
Hologram                    = default("Hologram",                      ::HologramShader)

Monochrome                  = default("Monochrome",                    ::CommonShader)
InvertColor                 = default("InvertColor",                   ::CommonShader)
TvStatic                    = default("TvStatic",                      ::CommonShader,     tryCompatible = true)
Pulse                       = default("Pulse",                         ::CommonShader)
// Progressed
InvertingColorRGB           = default("InvertingColorRgb",             ::ProgressShader)
InvertingColorRbg2HsvInHsv  = default("InvertingColorRgb2HsvInHsv",    ::ProgressShader)
InvertingColorRbg2HsvInRgb  = default("InvertingColorRgb2HsvInRgb",    ::ProgressShader)
Monochromize                = default("Monochromize",                  ::ProgressShader)
// Block Surface
Cyberion                    = screen("Cyberion",                       ::SurfaceShader)
//TestShieldScreen            = screen("TestShield",                     ::TestShieldShader)
//TestScreen                  = screen("Hologram",                       ::HologramShader)
        // @formatter:on
    }

    val String.fragFileInCio: FragFi
        get() = Vars.tree.get(R.SD.GenFrag(this.removeSuffix(".frag")))

    inline fun <T : ShaderBase> default(
        fragName: String,
        ctor: ShaderCtor<T>,
        tryCompatible: Boolean = false,
    ) = wrap("default", fragName, ctor, tryCompatible)

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
                fragment = preprocessFragment(fragFile)
            } catch (_: Exception) {
            }
            throw ShaderCompileException(
                "Can't compile shaders $vertFileName and $fragFileName\n$fragment\n", e)
        }
    }
    @JvmStatic
    @ClientOnly
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

private typealias FragFi = Fi
private typealias VertFi = Fi
private typealias ShaderCtor<T> = (FragFi, VertFi) -> T

val String.compatible: String
    get() = if (CioMod.TestGlCompatibility || this.useCompatible)
        "$this-compatible"
    else
        this
