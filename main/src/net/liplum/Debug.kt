@file:Suppress("MoveLambdaOutsideParentheses")

package net.liplum

import net.liplum.lib.shaders.FragFi
import net.liplum.lib.shaders.SD
import net.liplum.lib.shaders.ShaderBase
import net.liplum.lib.shaders.VertFi
import net.liplum.mdt.shaders.CommonShader
import net.liplum.shaders.SurfaceShader
import net.liplum.shaders.holo.HologramShader

object Debug {
    var enableUnlockContent = false
    var testText = ""
    var settings: List<Setting<out Any>> = listOf(
        Setting(
            "Enable Unlock Content",
            ::enableUnlockContent::get,
            { enableUnlockContent = it },
            SettingType.Check,
        )
    )

    enum class SettingType {
        Check, Text, SliderBar
    }

    class Setting<T>(
        val name: String,
        val getter: () -> T,
        val setter: (T) -> Unit,
        val type: SettingType
    )

    var shaders = listOf(
        Shader(
            ShaderType(
                "Hologram",
                { v, f -> HologramShader(v, f) },
                VertShaderType.Default
            ),
            { SD.Hologram = it as HologramShader }
        ),
        Shader(
            ShaderType(
                "TvStatic",
                { v, f -> CommonShader(v, f) },
                VertShaderType.Default
            ),
            { SD.TvStatic = it as CommonShader }
        ),
        Shader(
            ShaderType(
                "Cyberion",
                { v, f -> SurfaceShader(v, f) },
                VertShaderType.ScreenSpace
            ),
            { SD.Cyberion = it as SurfaceShader }
        )
    )

    class Shader(
        val type: ShaderType,
        val shaderSetter: (ShaderBase) -> Unit
    )

    enum class VertShaderType(
        val filePath: String
    ) {
        Default("default.vert"),
        ScreenSpace("screenspace.vert");
    }

    class ShaderType(
        val name: String,
        val ctor: (VertFi, FragFi) -> ShaderBase,
        val vertShaderType: VertShaderType
    )
}