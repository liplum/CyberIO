package net.liplum.registries

import mindustry.graphics.CacheLayer
import mindustry.graphics.CacheLayer.ShaderLayer
import net.liplum.ClientOnly

object CioCLs {
    private var registered = false
    private lateinit var allCacheLayers: ArrayList<CacheLayer>
    @JvmStatic
    fun load() {
        allCacheLayers = ArrayList()
        registerAll()
    }

    @JvmStatic
    fun registerAll() {
        if (!registered) {
            CacheLayer.add(*allCacheLayers.toTypedArray())
            registered = true
        }
    }

    fun <T> T.register(): T where T : CacheLayer {
        allCacheLayers.add(this)
        return this
    }
}
