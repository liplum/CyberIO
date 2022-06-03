package net.liplum.ui

import arc.Core
import arc.graphics.Color
import arc.math.Interp
import arc.scene.actions.Actions
import arc.scene.ui.ImageButton
import arc.scene.ui.ImageButton.ImageButtonStyle
import arc.scene.ui.Label
import arc.scene.ui.layout.Table
import mindustry.Vars
import mindustry.gen.Icon
import mindustry.gen.Sounds
import mindustry.gen.Tex
import mindustry.ui.dialogs.BaseDialog
import net.liplum.*
import net.liplum.ContentSpecXInfo.Companion.color
import net.liplum.lib.utils.bundle
import net.liplum.mdt.ClientOnly
import net.liplum.lib.ui.UIToast
import net.liplum.mdt.ui.addTrackTooltip

@ClientOnly
object ContentSpecDialog {
    val prefix = "setting.${R.Setting.ContentSpecific}"
    var toastUI = UIToast().apply {
        background = Tex.button
    }
    var fadeDuration = 0.8f
    @JvmStatic
    fun bundle(key: String, vararg args: Any) =
        if (args.isEmpty()) "$prefix.$key".bundle
        else "$prefix.$key".bundle(*args)
    @JvmStatic
    fun show() {
        var curSpec = Var.ContentSpecific
        var changed = curSpec != Var.ContentSpecific
        fun changeCurSpec(new: ContentSpec) {
            if (curSpec != new) {
                curSpec = new
                Sounds.message.play()
                val tipKey = if (curSpec != Var.ContentSpecific) "switch-to" else "switch-back"
                toastUI.postToastOnUI(Table().apply {
                    add(bundle(tipKey, curSpec.i18nName))
                })
                changed = true
            }
        }

        fun hasUnsavedChange() =
            curSpec != Var.ContentSpecific

        BaseDialog(bundle("title")).apply {
            cont.add(Table().apply {
                add(Label(R.Bundle.UnsavedChange.bundle).apply {
                    setColor(R.C.RedAlert)
                })
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
                val default = Core.scene.getStyle(ImageButtonStyle::class.java)
                val style = ImageButtonStyle(default).apply {
                    checked = default.over
                }
                ContentSpec.values().forEach {
                    this.add(ImageButton(it.icon, style).apply {
                        clicked {
                            changeCurSpec(it)
                        }
                        update {
                            isChecked = it == curSpec
                        }
                        row()
                        add(Label(it.i18nName).apply {
                            update {
                                if (it == curSpec) setColor(it.color)
                                else setColor(Color.white)
                            }
                        })
                        addTrackTooltip(it.i18nDesc)
                    }).pad(5f)
                }
            })
            addCloseButton()
            run {
                buttons.button("@save", Icon.save) {
                    setSpec(curSpec)
                    this.hide()
                }.get().apply {
                    update {
                        isDisabled = !hasUnsavedChange()
                    }
                }
            }
        }.show()
    }
    @JvmStatic
    fun setSpec(spec: ContentSpec) {
        if (Var.ContentSpecific == spec) return
        Settings.ContentSpecific = spec.id
        Vars.ui.showInfoOnHidden(bundle("restart", spec.i18nName)) {
            Core.app.exit()
        }
    }
}