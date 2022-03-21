package net.liplum.registries

import arc.graphics.gl.Shader
import mindustry.Vars
import net.liplum.CioMod.TestMobileOnly
import net.liplum.ClientOnly
import net.liplum.shaders.BlockShader
import net.liplum.shaders.ILoadResource
import net.liplum.shaders.TrShader
import net.liplum.shaders.holo.Hologram
import net.liplum.shaders.holo.Hologram2
import java.util.*

object CioShaders {
    @ClientOnly lateinit var dynamicColor: Shader
    @ClientOnly lateinit var hologram: Hologram
    @ClientOnly lateinit var monochrome: TrShader
    @ClientOnly lateinit var invertColor: TrShader
    @ClientOnly lateinit var tvSnow: TrShader
    @ClientOnly lateinit var hologram2: Hologram2
    @ClientOnly
    private var allShaders: LinkedList<Shader> = LinkedList()
    private var allLoadable: LinkedList<ILoadResource> = LinkedList()
    private var isInited = false
    @JvmStatic
    fun init() {
        ClientOnly {
            allShaders = LinkedList()
            allLoadable = LinkedList()
            dynamicColor = BlockShader("dynamic-color").register()
            hologram = Hologram("hologram").register()
            monochrome = TrShader("monochrome").register()
            invertColor = TrShader("invert-color").register()
            tvSnow = TrShader("tv-static".compatible).register()
            hologram2 = Hologram2("hologram2").register()
            isInited = true
        }
    }
    @JvmStatic
    fun loadResource() {
        ClientOnly {
            if (isInited) {
                for (loadable in allLoadable) {
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
                for (shader in allShaders) {
                    shader.dispose()
                }
            }
        }
    }
    @ClientOnly
    fun <T> T.register(): T where T : Shader {
        allShaders.add(this)
        if (this is ILoadResource) {
            allLoadable.add(this)
        }
        return this
    }
}

val String.compatible: String
    get() = if (Vars.mobile || Vars.testMobile || TestMobileOnly)
        "$this-mobile"
    else
        this
