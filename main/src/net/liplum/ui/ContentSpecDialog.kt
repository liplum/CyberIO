package net.liplum.ui

import arc.Core
import arc.math.Interp
import arc.scene.actions.Actions
import arc.scene.ui.CheckBox
import arc.scene.ui.ImageButton
import arc.scene.ui.Label
import arc.scene.ui.layout.Table
import mindustry.Vars
import mindustry.gen.Icon
import mindustry.ui.dialogs.BaseDialog
import net.liplum.*
import net.liplum.lib.utils.bundle
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.ui.UIToast
import net.liplum.mdt.ui.addTrackTooltip

@ClientOnly
object ContentSpecDialog {
    val prefix = "setting.${R.Setting.ContentSpecific}"
    var toastUI = UIToast()
    @JvmStatic
    fun bundle(key: String, vararg args: Any) =
        if (args.isEmpty()) "$prefix.$key".bundle
        else "$prefix.$key".bundle(*args)
    @JvmStatic
    fun show() {
        var curSpec = CioMod.ContentSpecific
        var changed = curSpec != CioMod.ContentSpecific
        fun changeCurSpec(new: ContentSpec) {
            if (curSpec != new) {
                curSpec = new
                val tipKey = if (curSpec != CioMod.ContentSpecific) "switch-to" else "switch-back"
                toastUI.postToastOnUI(Table().apply {
                    add(bundle(tipKey, curSpec.i18nName))
                })
                changed = true
            }
        }

        fun hasUnsavedChange() =
            curSpec != CioMod.ContentSpecific
        BaseDialog(bundle("title")).apply {
            cont.add(Table().apply {
                add(Label(R.Bundle.UnsavedChange.bundle).apply {
                    setColor(R.C.RedAlert)
                })
                val fadeDuration = 1f
                actions(Actions.alpha(0f))
                update {
                    if (!changed) return@update
                    if (hasUnsavedChange()) {
                        changed = false
                        actions.clear()
                        actions(
                            Actions.alpha(0f),
                            Actions.fadeIn(fadeDuration, Interp.fade)
                        )
                    } else {
                        changed = false
                        actions.clear()
                        actions(
                            Actions.fadeOut(fadeDuration, Interp.fade)
                        )
                    }
                }
            }).row()
            cont.add(bundle("introduction")).row()
            cont.add(Table().apply {
                ContentSpec.values().forEach {
                    this.add(Table().apply {
                        add(ImageButton(it.icon).apply {
                            changed {
                                changeCurSpec(it)
                            }
                            addTrackTooltip(it.i18nDesc)
                        }).row()
                        add(CheckBox(it.i18nName).apply {
                            changed {
                                changeCurSpec(it)
                            }
                            update {
                                isChecked = it == curSpec
                            }
                        })
                    }).pad(5f)
                }
            })
            addCloseButton()
            run {
                buttons.defaults().size(210f, 64f)
                buttons.button("@save", Icon.save) {
                    setSpec(curSpec)
                    this.hide()
                }.size(210f, 64f)
            }
        }.show()
    }
    @JvmStatic
    fun setSpec(spec: ContentSpec) {
        if (CioMod.ContentSpecific == spec) return
        Settings.ContentSpecific = spec.id
        Vars.ui.showInfoOnHidden(bundle("restart", spec.i18nName)) {
            Core.app.exit()
        }
    }
}