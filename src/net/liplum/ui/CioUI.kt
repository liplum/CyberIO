package net.liplum.ui

import arc.Core
import arc.Events
import arc.math.Interp
import arc.scene.ui.TextButton
import arc.scene.ui.TextField
import arc.scene.ui.layout.Table
import arc.scene.utils.Elem
import arc.util.Http
import mindustry.Vars
import mindustry.game.EventType.Trigger
import mindustry.ui.Styles
import mindustry.ui.dialogs.BaseDialog
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.CheckSetting
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.SliderSetting
import net.liplum.Meta
import net.liplum.R
import net.liplum.Settings
import net.liplum.UnsteamOnly
import net.liplum.lib.ui.ShowTextDialog
import net.liplum.lib.ui.addTrackTooltip
import net.liplum.lib.ui.settings.*
import net.liplum.lib.ui.settings.SliderSettingX.Companion.addSliderSettingX
import net.liplum.utils.bundle
import net.liplum.utils.getF
import net.liplum.utils.invoke
import net.liplum.utils.useFakeHeader

object CioUI {
    @JvmStatic
    fun appendSettings() {
        addCyberIOSettingMenu()
    }
    @JvmStatic
    fun addCyberIOSettingMenu() {
        val uiSettings = Vars.ui.settings
        val menu = uiSettings.getF<Table>("menu")
        val prefs = uiSettings.getF<Table>("prefs")
        uiSettings.resized {
            settings.rebuild()
            uiSettings.updateScrollFocus()
        }
        Events.run(Trigger.update) {
            if (Vars.ui.settings.isShown) {
                val cioSettings = menu.find<TextButton>(SettingButtonName)
                if (cioSettings == null) {
                    menu.row()
                    menu.button(Meta.Name, Styles.cleart) {
                        prefs.clearChildren()
                        prefs.add(settings)
                    }.get().apply {
                        name = SettingButtonName
                    }
                }
            }
        }
    }

    const val SettingButtonName = "cyber-io-settings-button"
    val settings = SettingsTableX().apply {
        genHeader = {
            it.add("${Meta.Name} v${Meta.DetailedVersion}").row()
        }
        addSliderSettingX(R.Setting.LinkOpacity,
            100, 0, 100, 5, { "$it%" }
        ) {
            Settings.LinkOpacity = Core.settings.getInt(R.Setting.LinkOpacity) / 100f
        }
        // input [0,100] -> output [0,30]
        val pct2Density: (Int) -> Float = {
            Interp.pow2Out(it / 100f) * 30f
        }
        addSliderSettingX(R.Setting.LinkArrowDensity,
            15, 0, 100, 5, { "$it" }
        ) {
            Settings.LinkArrowDensity = pct2Density(Core.settings.getInt(R.Setting.LinkArrowDensity, 15))
        }
        val alwaysShowLinkDefault = Vars.mobile
        addCheckPref(R.Setting.AlwaysShowLink, alwaysShowLinkDefault) {
            Settings.AlwaysShowLink = Core.settings.getBool(R.Setting.AlwaysShowLink, alwaysShowLinkDefault)
        }
        addSliderSettingX(
            R.Setting.LinkSize,
            100, 0, 100, 5, { "$it%" }) {
            Settings.LinkSize = Core.settings.getInt(R.Setting.LinkSize, 100) / 100f * 4f
        }
        addCheckPref(R.Setting.ShowLinkCircle, alwaysShowLinkDefault) {
            Settings.ShowLinkCircle = Core.settings.getBool(R.Setting.ShowLinkCircle, true)
        }
        UnsteamOnly {
            addCheckPref(
                R.Setting.ShowUpdate, !Vars.steam
            )
        }
        addCheckPref(
            R.Setting.ShowWelcome, true
        )
        UnsteamOnly {
            addAny {
                val prefix = "setting.${R.Setting.GitHubMirrorUrl}"
                fun bundle(key: String, vararg args: Any) =
                    if (args.isEmpty()) "$prefix.$key".bundle
                    else "$prefix.$key".bundle(*args)

                val button = Elem.newButton(bundle("button")) {
                    val dialog = BaseDialog(bundle("dialog")).apply {
                        val field = TextField(
                            Settings.GitHubMirrorUrl, Styles.defaultField
                        ).addTrackTooltip(bundle("field-tooltip"))
                        onSettingsReset {
                            Settings.GitHubMirrorUrl = Meta.GitHubMirrorUrl
                            field.text = Meta.GitHubMirrorUrl
                        }
                        cont.add(field).width((Core.graphics.width / 1.2f).coerceAtMost(460f)).row()
                        cont.table { t ->
                            fun onFailed(error: String) {
                                ShowTextDialog(bundle("failed", field.text, error))
                            }

                            fun onSucceeded() {
                                val url = field.text.trim('\\').trim('/')
                                Settings.GitHubMirrorUrl = url
                                field.text = url
                                ShowTextDialog(bundle("success", url))
                            }

                            fun onResetDefault() {
                                Settings.GitHubMirrorUrl = Meta.GitHubMirrorUrl
                                field.text = Meta.GitHubMirrorUrl
                                ShowTextDialog(bundle("reset", field.text))
                            }

                            val saveButton = TextButton("@save").apply {
                                changed {
                                    if (field.text.isEmpty()) {
                                        onResetDefault()
                                    } else {
                                        isDisabled = true
                                        Http.get(field.text).useFakeHeader().error { e ->
                                            onFailed("${e.javaClass.name} ${e.message}")
                                            isDisabled = false
                                        }.submit { rep ->
                                            Core.app.post {
                                                if (rep.status == Http.HttpStatus.OK)
                                                    onSucceeded()
                                                else
                                                    onFailed(rep.status.name)
                                                isDisabled = false
                                            }
                                        }
                                    }
                                }
                                addTrackTooltip(bundle("save-tooltip"))
                            }
                            t.add(saveButton).size(200f, 50f)
                            t.button("@cancel") {
                                hide()
                            }.size(200f, 50f).get().apply {
                                addTrackTooltip(bundle("cancel-tooltip"))
                            }
                        }
                        cont.row()
                        addCloseListener()
                    }
                    dialog.show()
                }.addTrackTooltip(bundle("button-tooltip"))
                it.add(button).fillX()
            }
        }
        sortBy {
            when (it) {
                is SliderSetting -> 0
                is SliderSettingX -> 1
                is CheckSetting -> 2
                is CheckSettingX -> 3
                is AnySetting -> 4
                else -> Int.MAX_VALUE
            }
        }
    }
}