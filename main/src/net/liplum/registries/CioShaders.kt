package net.liplum.registries

import arc.files.Fi
import arc.graphics.gl.Shader
import mindustry.Vars
import net.liplum.CioMod
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.lib.shaders.*
import net.liplum.shaders.SurfaceShader
import net.liplum.shaders.holo.Hologram
import net.liplum.shaders.holo.HologramOld
import net.liplum.useCompatible

@ClientOnly
object CioShaders {
    // @formatter:off
    lateinit var DynamicColor:                  TrShader
    lateinit var Hologram:                      Hologram
    lateinit var Monochrome:                    TrShader
    lateinit var InvertColor:                   TrShader
    lateinit var TvStatic:                      TrShader
    var Cyberion:                               SurfaceShader? = null
    // @formatter:on
    @JvmStatic
    @ClientOnly
    fun init() {
        // @formatter:off
// Dynamic
DynamicColor                = wrap("DynamicColor",                  ::TrShader)
// Hologram
Hologram                    = wrap("Hologram",                      ::Hologram)

Monochrome                  = wrap("Monochrome",                    ::TrShader)
InvertColor                 = wrap("InvertColor",                   ::TrShader)
TvStatic                    = wrap("TvStatic",                      ::TrShader,     tryCompatible = true)
// Progressed
// Block Surface
Cyberion                    = wrap("Cyberion",                      ::SurfaceShader)
        // @formatter:on
    }

    val String.filePath: String
        get() = R.SD.GenFrag(this)

    inline fun <T : Shader> wrap(
        name: String,
        ctor: (Fi) -> T,
        tryCompatible: Boolean = false,
    ): T {
        val fragName = (if (tryCompatible) name.compatible else name).filePath
        val file = Vars.tree.get(fragName)
        try {
            val shader = ctor(file)
            return shader.register()
        } catch (e: Exception) {
            val fragment = preprocessFragment(file)
            throw ShaderCompileException(
                "Can't compile shader $fragName\n$fragment\n", e)
        }
    }
    @JvmStatic
    @ClientOnly
    fun loadResource() {
        for (loadable in AllLoadable) {
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
    fun <T> T.register(): T where T : Shader {
        AllShaders.add(this)
        if (this is ILoadResource) {
            AllLoadable.add(this)
        }
        return this
    }

    private var AllShaders: HashSet<Shader> = HashSet()
    private var AllLoadable: HashSet<ILoadResource> = HashSet()
}

val String.compatible: String
    get() = if (CioMod.TestGlCompatibility || this.useCompatible)
        "$this-compatible"
    else
        this
