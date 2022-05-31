@file:Suppress("MoveLambdaOutsideParentheses")

package net.liplum

import net.liplum.lib.shaders.SD
import net.liplum.lib.shaders.ShaderBase
import net.liplum.mdt.shaders.CommonShader
import net.liplum.registries.ShaderCtor
import net.liplum.shaders.HologramShader
import net.liplum.shaders.HologramizeShader
import net.liplum.shaders.SurfaceShader
import net.liplum.shaders.VanishingShader

object Debug {
    var enableUnlockContent = false
    var testText = ""
    var settings: List<Setting<out Any>> = listOf(
        Setting(
            "Enable Unlock Content",
            ::enableUnlockContent::get,
            { enableUnlockContent = it },
            SettingType.Check,
        ),
        Setting(
            "Enable Debug Mode",
            { CioMod.DebugMode },
            { CioMod.DebugMode = it },
            SettingType.Check,
        ),
        Setting(
            "Show Power Graph ID",
            { Var.ShowPowerGraphID },
            { Var.ShowPowerGraphID = it },
            SettingType.Check,
        ),
        Setting(
            "Show Blocks' HitBox",
            { Var.DrawBuildCollisionRect },
            { Var.DrawBuildCollisionRect = it },
            SettingType.Check,
        ),
        Setting(
            "Show Units' HitBox",
            { Var.DrawUnitCollisionRect },
            { Var.DrawUnitCollisionRect = it },
            SettingType.Check,
        ),
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
        val ctor: ShaderCtor<ShaderBase>,
        val vertShaderType: VertShaderType
    )
}