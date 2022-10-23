@file:Suppress("MoveLambdaOutsideParentheses")

package net.liplum

import net.liplum.common.CoerceLength
import net.liplum.common.shader.ShaderBase
import net.liplum.registry.SD
import net.liplum.registry.ShaderCtor
import net.liplum.shaders.CommonShader
import net.liplum.shaders.HologramShader
import net.liplum.shaders.HologramizeShader
import net.liplum.shaders.SurfaceShader
import net.liplum.shaders.VanishingShader

object Debug {
    var testText = ""
    var settings: List<Setting<out Any>> = listOf(
        Setting(
            { "Debug:${Var.CurDebugLevel}".CoerceLength(15) },
            { Var.CurDebugLevel.level.toFloat() * (100f / DebugLevel.size) },
            {
                val old = Var.CurDebugLevel
                Var.CurDebugLevel = DebugLevel.valueOf((it / (100f / DebugLevel.size)).toInt())
                if (old != Var.CurDebugLevel)
                    CLog.info("Debug Level is changed to ${Var.CurDebugLevel}")
            },
            SettingType.SliderBar,
        ),
        Setting(
            { "Show Power Graph ID" },
            { Var.ShowPowerGraphID },
            { Var.ShowPowerGraphID = it },
            SettingType.Check,
        ),
        Setting(
            { "Show Blocks' HitBox" },
            { Var.DrawBuildCollisionRect },
            { Var.DrawBuildCollisionRect = it },
            SettingType.Check,
        ),
        Setting(
            { "Show Units' HitBox" },
            { Var.DrawUnitCollisionRect },
            { Var.DrawUnitCollisionRect = it },
            SettingType.Check,
        ),
        Setting(
            { "Entity Inspector" },
            { Var.EnableEntityInspector },
            { Var.EnableEntityInspector = it },
            SettingType.Check,
        ),
    )

    enum class SettingType {
        Check, Text,
        /**
         * Range: `[0f,100f]`
         */
        SliderBar
    }

    class Setting<T>(
        val name: () -> String,
        val getter: () -> T,
        val setter: (T) -> Unit,
        val type: SettingType,
    )

    var shaders = listOf(
        Shader(
            ShaderType(
                "Hologram",
                ::HologramShader,
                VertShaderType.Default
            ),
            { SD.Hologram = it as HologramShader }
        ),
        Shader(
            ShaderType(
                "TvStatic",
                ::CommonShader,
                VertShaderType.Default
            ),
            { SD.TvStatic = it as CommonShader }
        ),
        Shader(
            ShaderType(
                "Cyberion",
                ::SurfaceShader,
                VertShaderType.ScreenSpace
            ),
            { SD.Cyberion = it as SurfaceShader }
        ),
        Shader(
            ShaderType(
                "Hologramize",
                { v, f -> HologramizeShader(v, f) },
                VertShaderType.Default
            ),
            { SD.Hologramize = it as HologramizeShader }
        ),
        Shader(
            ShaderType(
                "Vanishing",
                { v, f -> VanishingShader(v, f) },
                VertShaderType.Default
            ),
            { SD.Vanishing = it as VanishingShader }
        )
    )

    class Shader(
        val type: ShaderType,
        val shaderSetter: (ShaderBase) -> Unit,
    )

    enum class VertShaderType(
        val filePath: String,
    ) {
        Default("default.vert"),
        ScreenSpace("screenspace.vert");
    }

    class ShaderType(
        val name: String,
        val ctor: ShaderCtor<ShaderBase>,
        val vertShaderType: VertShaderType,
    )
}