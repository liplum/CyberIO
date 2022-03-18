package net.liplum.registries

import arc.graphics.gl.Shader
import net.liplum.ClientOnly
import net.liplum.shaders.BlockShader
import net.liplum.shaders.HologramShader
import net.liplum.shaders.ILoadResource
import net.liplum.shaders.TrShader
import java.util.*

object CioShaders {
    @ClientOnly lateinit var dynamicColor: Shader
    @ClientOnly lateinit var hologram: HologramShader
    @ClientOnly lateinit var monochrome: TrShader
    @ClientOnly lateinit var invertColor: TrShader
    @ClientOnly lateinit var tvSnow: TrShader
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
            hologram = HologramShader("hologram").register()
            monochrome = TrShader("monochrome").register()
            invertColor = TrShader("invert-color").register()
            tvSnow = TrShader("tv-static").register()
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
