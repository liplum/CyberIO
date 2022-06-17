package net.liplum.ui

import arc.Core
import arc.graphics.Color
import arc.math.Interp
import arc.scene.actions.Actions
import arc.scene.ui.ImageButton
import arc.scene.ui.ImageButton.ImageButtonStyle
import arc.scene.ui.Label
import arc.scene.ui.layout.Table
import arc.util.Align
import mindustry.Vars
import mindustry.gen.Icon
import mindustry.gen.Sounds
import mindustry.gen.Tex
import net.liplum.*
import net.liplum.ContentSpecXInfo.Companion.color
import net.liplum.common.ui.UIToast
import net.liplum.common.utils.IBundlable
import net.liplum.common.utils.bundle
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.ui.addTrackTooltip
import net.liplum.ui.templates.NewIconTextButton

@ClientOnly
object ContentSpecFrag : IBundlable {
    override val bundlePrefix = "setting.${R.Setting.ContentSpecific}"
    var toastUI = UIToast().apply {
        background = Tex.button
    }
    val title: String
        get() = bundle("title")
    var fadeDuration = 0.8f
    @JvmStatic
    fun build(cont: Table) {
        // Main
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
        // Tip
        cont.add(Table().apply {
            add(Table().apply {
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
            // Options
            add(bundle("introduction")).row()
            add(Table().apply {
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
        }).grow()
        cont.row()
        // Buttons
        cont.add(Table().apply {
            add(NewIconTextButton("@save", Icon.save) {
                setSpec(curSpec)
            }).then {
                update {
                    isDisabled = !hasUnsavedChange()
                }
                align(Align.bottom)
            }.width(200f).row()
        })
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