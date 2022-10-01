package net.liplum

import arc.Core
import arc.graphics.gl.GLVersion
import arc.graphics.gl.GLVersion.GlType.GLES
import arc.graphics.gl.GLVersion.GlType.OpenGL
import net.liplum.CLog.log
import net.liplum.Compatible.Hologram
import net.liplum.Compatible.TvStatic
import plumy.core.ClientOnly

@ClientOnly
object GL {
    @JvmStatic
    val GlVersion: GLVersion = Core.graphics.glVersion
    @JvmStatic
    @ClientOnly
    fun handleCompatible() {
        Hologram = false
        TvStatic = useCompatible(
            LessThan(GLES, 3, 2),
            LessThan(OpenGL, 3, 1)
        )

        CompatibleMap.log("${Meta.Name} Compatible") { name, func ->
            CLog.info("$name|${func()}")
        }
    }

    fun useCompatible(A: Int, B: Int) =
        GlVersion.majorVersion <= A && GlVersion.minorVersion <= B
    @JvmStatic
    fun useCompatible(vararg entries: LessThan): Boolean {
        for (entry in entries) {
            if (entry.useCompatible(GlVersion.type, GlVersion.majorVersion, GlVersion.minorVersion))
                return true
        }
        return false
    }
    /**
     * @param atLeastA Including itself
     * @param atLeastB Including itself
     */
    class LessThan(
        val type: GLVersion.GlType, val atLeastA: Int, val atLeastB: Int,
    ) {
        fun useCompatible(type: GLVersion.GlType, A: Int, B: Int) =
            this.type == type && A <= atLeastA && B <= atLeastB
    }
}

