package net.liplum.ui

import arc.Core
import arc.Events
import arc.math.Interp
import arc.scene.ui.Dialog
import arc.scene.ui.TextButton
import arc.scene.ui.TextField
import arc.scene.ui.layout.Table
import arc.scene.utils.Elem
import arc.util.Align
import arc.util.Http
import kotlinx.coroutines.launch
import mindustry.Vars
import mindustry.core.GameState.State.menu
import mindustry.game.EventType
import mindustry.game.EventType.Trigger
import mindustry.gen.Sounds
import mindustry.ui.Styles
import mindustry.ui.dialogs.BaseDialog
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.CheckSetting
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.SliderSetting
import net.liplum.*
import net.liplum.lib.bundle
import net.liplum.lib.getF
import net.liplum.lib.ing
import net.liplum.lib.ui.ShowTextDialog
import net.liplum.lib.ui.addTrackTooltip
import net.liplum.lib.ui.settings.*
import net.liplum.lib.ui.settings.AnySetting.Companion.addAny
import net.liplum.lib.ui.settings.CheckSettingX.Companion.addCheckPref
import net.liplum.lib.ui.settings.SliderSettingX.Companion.addSliderSettingX
import net.liplum.mdt.advanced.MapCleaner
import net.liplum.update.Updater
import net.liplum.utils.invoke
import net.liplum.utils.randomExcept
import net.liplum.utils.useFakeHeader
import net.liplum.welcome.Conditions
import net.liplum.welcome.Welcome
import net.liplum.welcome.WelcomeList

object CioUI {
    @JvmStatic
    fun appendSettings() {
        addCyberIOSettingMenu()
    }
    @JvmStatic
    @UseReflection
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
        var isMenu = true
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
            ).apply {
                canShow = { isMenu }
            }
        }
        addCheckPref(
            R.Setting.ShowWelcome, true
        ).apply {
            canShow = { isMenu }
        }
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
                                update {
                                    label.setText(if (isDisabled) R.Ctrl.Validate.bundle.ing else "@save")
                                }
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
                                field.text = Settings.GitHubMirrorUrl
                            }.size(200f, 50f).get().apply {
                                addTrackTooltip(bundle("cancel-tooltip"))
                            }
                        }
                        cont.row()
                        addCloseButton()
                    }
                    dialog.show()
                }.addTrackTooltip(bundle("button-tooltip")).apply {
                }
                it.add(button).fillX()
            }.apply {
                canShow = { isMenu }
            }
            addAny {
                fun bundle(key: String) = "setting.${R.Setting.CheckUpdate}.$key".bundle
                val buttonI18n = bundle("button")
                val button = TextButton(buttonI18n).apply {
                    update {
                        label.setText(if (isDisabled) buttonI18n.ing else buttonI18n)
                    }
                    changed {
                        var failed: String? = null
                        Updater.fetchLatestVersion(onFailed = { e ->
                            failed = e
                        })
                        isDisabled = true
                        Updater.launch {
                            Updater.accessJob?.join()
                            val errorInfo = failed
                            if (errorInfo != null) {
                                showUpdateFailed(errorInfo)
                            } else {
                                if (Updater.requireUpdate) {
                                    val updateTips = WelcomeList.findAll { tip ->
                                        tip.condition == Conditions.CheckUpdate
                                    }
                                    if (updateTips.isEmpty()) {
                                        ShowTextDialog(bundle("not-support"))
                                    } else {
                                        val updateTip = updateTips.randomExcept(atLeastOne = true) {
                                            id != Settings.LastWelcomeID
                                        }
                                        if (updateTip != null)
                                            Welcome.genEntity().apply {
                                                tip = updateTip
                                            }.showTip()
                                        else
                                            ShowTextDialog(bundle("not-support"))
                                    }
                                } else {
                                    ShowTextDialog(bundle("already-latest"))
                                }
                            }
                            isDisabled = false
                        }
                    }
                }
                it.add(button).fillX()
            }.apply {
                canShow = { isMenu }
            }
        }
        addAny {
            it.add(TextButton(AdvancedFunctionDialog.bundle("title")).apply {
                clicked {
                    AdvancedFunctionDialog.show()
                }
            }).fillX()
        }.apply {
            canShow = { isMenu }
        }
        addAny {
            it.add(TextButton(R.Advanced.MapCleaner.bundle).apply {
                clicked {
                    MapCleaner.cleanCurrentMap(Meta.ModID)
                    Sounds.message.play()
                }
                update {
                    isDisabled = !(Var.EnableMapCleaner && !isMenu && OnlyLocal())
                }
            }).fillX()
        }.apply {
            canShow = { Var.EnableMapCleaner && !isMenu && OnlyLocal() }
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
        Events.on(EventType.StateChangeEvent::class.java) {
            val lastIsMenu = isMenu
            isMenu = it.to == menu
            if (lastIsMenu != isMenu) {
                rebuild()
            }
        }
    }

    fun showUpdateFailed(error: String) {
        Dialog().apply {
            getCell(cont).growX()
            cont.margin(15f).add(
                R.Ctrl.UpdateModFailed.bundle(error)
            ).width(400f).wrap().get().setAlignment(Align.center, Align.center)
            buttons.button("@ok") {
                this.hide()
            }.size(110f, 50f).pad(4f)
            closeOnBack()
        }.show()
    }
}