package net.liplum

import arc.Core
import arc.graphics.gl.GLVersion
import arc.graphics.gl.GLVersion.GlType.GLES
import arc.graphics.gl.GLVersion.GlType.OpenGL
import arc.util.Log
import net.liplum.Clog.log
import net.liplum.Compatible.*

object GL {
    @JvmStatic
    val GlVersion: GLVersion = Core.graphics.glVersion
    @JvmStatic
    fun handleCompatible() {
        Hologram = false
        TvStatic = match(GLES, 3, 2)
        HoloForceField = match(OpenGL, 2, 2) || match(GLES, 2, 0)

        CompatibleMap.log("${Meta.Name} Compatible") { name, func ->
            Log.info("$name|${func()}")
        }
    }
    @JvmStatic
    fun match(A: Int, B: Int) =
        GlVersion.majorVersion <= A && GlVersion.minorVersion <= B
    @JvmStatic
    fun match(type: GLVersion.GlType, A: Int, B: Int) =
        GlVersion.type == type && GlVersion.majorVersion <= A && GlVersion.minorVersion <= B
}

