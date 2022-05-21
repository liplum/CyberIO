package net.liplum.ui

import arc.Core
import arc.scene.ui.CheckBox
import arc.scene.ui.ImageButton
import arc.scene.ui.layout.Table
import mindustry.Vars
import mindustry.gen.Icon
import mindustry.ui.dialogs.BaseDialog
import net.liplum.CioMod
import net.liplum.ContentSpec
import net.liplum.R
import net.liplum.Settings
import net.liplum.lib.utils.bundle
import net.liplum.mdt.ui.addTrackTooltip

object ContentSpecDialog {
    val prefix = "setting.${R.Setting.ContentSpecific}"
    @JvmStatic
    fun bundle(key: String, vararg args: Any) =
        if (args.isEmpty()) "$prefix.$key".bundle
        else "$prefix.$key".bundle(*args)
    @JvmStatic
    fun show() {
        var curSpec = CioMod.ContentSpecific
        BaseDialog(bundle("title")).apply {
            cont.add(Table().apply {
                ContentSpec.values().forEach {
                    this.add(Table().apply {
                        add(ImageButton(it.icon).apply {
                            changed {
                                curSpec = it
                            }
                            addTrackTooltip(it.i18nDesc)
                        }).row()
                        add(CheckBox(it.i18nName).apply {
                            changed {
                                curSpec = it
                            }
                            update {
                                isChecked = it == curSpec
                            }
                        })
                    }).pad(5f)
                }
            })
            run {
                buttons.defaults().size(210f, 64f)
                buttons.button("@save", Icon.save) {
                    setSpec(curSpec)
                    this.hide()
                }.size(210f, 64f)
            }
            addCloseButton()
        }.show()
    }
    @JvmStatic
    fun setSpec(spec: ContentSpec) {
        Settings.ContentSpecific = spec.id
        Vars.ui.showInfoOnHidden(bundle("restart", spec.i18nName)) {
            Core.app.exit()
        }
    }
}