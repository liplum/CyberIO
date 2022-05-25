package net.liplum.ui

import arc.files.Fi
import arc.scene.Element
import arc.scene.ui.ScrollPane
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Table
import arc.scene.utils.Elem
import mindustry.Vars
import mindustry.graphics.Pal
import mindustry.graphics.Shaders.getShaderFi
import mindustry.ui.dialogs.BaseDialog
import net.liplum.Debug
import net.liplum.Debug.SettingType
import net.liplum.S
import net.liplum.Settings
import net.liplum.lib.Dir
import net.liplum.lib.toFi
import net.liplum.mdt.ui.ShowTextDialog
import net.liplum.welcome.Welcome

object DebugSettingsDialog {
    @JvmStatic
    fun show() {
        BaseDialog("Debug Settings").apply {
            addCloseButton()
            cont.add(ScrollPane(Table().apply {
                add("Stuff").color(Pal.ammo).row()
                add(Table().apply {
                    add(TextButton("Show welcome").apply {
                        changed {
                            Welcome.showWelcomeDialog()
                        }
                    }).width(300f)
                }).pad(5f).row()
                add("Settings").color(Pal.bulletYellowBack).row()
                for (setting in Debug.settings) {
                    add(setting.resolveSettingType()).growX().row()
                }
                add("Shaders").color(S.HologramDark).row()
                add(Table().apply {
                    add("Root Folder:")
                    field(Settings.ShaderRootPath) {
                        Settings.ShaderRootPath = it
                    }.width(300f)
                }).pad(5f).row()
                val shaderLocator: (String) -> Fi = {
                    Dir(Settings.ShaderRootPath).subF(it).toFi()
                }
                for (shader in Debug.shaders) {
                    add(shader.resolveShader(shaderLocator)).growX().row()
                }
            }))
        }.show()
    }
    @Suppress("UNCHECKED_CAST")
    fun <T> Debug.Setting<T>.resolveSettingType(): Element = when (type) {
        SettingType.Check -> Elem.newCheck(name) { setter(it as T) }.apply {
            isChecked = getter() as? Boolean ?: false
        }
        SettingType.Text -> Table().apply {
            add(name)
            field(getter() as? String ?: "") {
                setter(it as T)
            }
        }
        SettingType.SliderBar -> Table().apply {
            add(name)
            slider(0f, 100f, 5f) {
                setter(it as T)
            }
        }
    }

    fun Debug.Shader.resolveShader(
        fragFiResolver: (String) -> Fi
    ): Element = Table().apply {
        add(type.name).pad(15f)
        add(TextButton("Reload").apply {
            changed {
                isDisabled = true
                val vertFi = getShaderFi(type.vertShaderType.filePath)
                val fragFi = fragFiResolver("${type.name}.frag")
                try {
                    val shader = type.ctor(vertFi, fragFi)
                    shaderSetter(shader)
                } catch (e: Exception) {
                    Vars.ui.showException(e)
                    return@changed
                } finally {
                    isDisabled = false
                }
                ShowTextDialog("${type.name} shader reloaded.")
            }
        }).width(100f)
    }
}