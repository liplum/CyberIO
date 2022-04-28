package net.liplum.welcome

import arc.Core
import arc.scene.ui.Dialog
import arc.scene.ui.Label
import arc.util.Align
import arc.util.Time
import mindustry.Vars
import mindustry.ui.dialogs.BaseDialog
import net.liplum.CioMod
import net.liplum.Meta
import net.liplum.R
import net.liplum.Settings
import net.liplum.update.Updater
import net.liplum.utils.bundle
import net.liplum.welcome.TemplateRegistry.register
import net.liplum.welcome.Welcome.Entity

object Templates {
    val Story = object : WelcomeTemplate("Story") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                cont.image(entity.icon)
                    .maxSize(200f).pad(20f).row()
                cont.add(Label(entity.bundle.format(
                    "welcome", Meta.DetailedVersion
                )).apply {
                    setAlignment(0)
                    setWrap(true)
                }).growX()
                    .row()
                cont.add(Label(entity.content).apply {
                    setAlignment(0)
                    setWrap(true)
                }).growX()
                    .row()
                cont.button(entity["read"]) {
                    Welcome.recordClick()
                    hide()
                }.size(200f, 50f)
            }
    }.register()
    val Update = object : WelcomeTemplate("Update") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                cont.image(entity.icon)
                    .maxSize(200f).pad(20f).row()
                cont.add(Label(entity.content.format(Updater.latestVersion)).apply {
                    setAlignment(0)
                    setWrap(true)
                }).growX()
                    .row()
                cont.table {
                    it.button(entity["yes"]) {
                        if (CioMod.jarFile != null) {
                            var progress = 0f
                            val loading = Vars.ui.loadfrag
                            loading.show("@downloading")
                            loading.setProgress { progress }
                            // Cache tips because updating successfully will replace codes and cause class not found exception.
                            val successTip = R.Ctrl.UpdateModSuccess.bundle(Updater.latestVersion)
                            Updater.updateSelfByReplace(onProgress = { p ->
                                progress = p
                            }, onSuccess = {
                                loading.hide()
                                Time.run(10f) {
                                    Vars.ui.showInfoOnHidden(successTip) {
                                        Core.app.exit()
                                    }
                                }
                            }, onFailed = { error ->
                                Core.app.post {
                                    loading.hide()
                                    Dialog("").apply {
                                        getCell(cont).growX()
                                        cont.margin(15f).add(
                                            R.Ctrl.UpdateModFailed.bundle(Updater.latestVersion, error)
                                        ).width(400f).wrap().get().setAlignment(Align.center, Align.center)
                                        buttons.button("@ok") {
                                            this.hide()
                                        }.size(110f, 50f).pad(4f)
                                        closeOnBack()
                                    }.show()
                                }
                            })
                        } else {
                            Updater.updateSelfByBuiltIn()
                        }
                        hide()
                    }.size(150f, 50f)
                    it.button(entity["no"]) {
                        hide()
                    }.size(150f, 50f)
                    it.button(entity["dont-show"]) {
                        Settings.ShowUpdate = false
                        hide()
                    }.size(150f, 50f)
                }.growX()
                    .row()
            }
    }.register()
    val OpenLink = object : WelcomeTemplate("OpenLink") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                cont.image(entity.icon)
                    .maxSize(200f).pad(20f).row()
                cont.add(Label(entity.content).apply {
                    setAlignment(0)
                    setWrap(true)
                }).growX()
                    .row()
                cont.table {
                    it.button(entity["yes"]) {
                        Core.app.openURI(entity["link"])
                        hide()
                    }.size(200f, 50f)
                    it.button(entity["no"]) {
                        hide()
                    }.size(200f, 50f)
                }.growX()
                    .row()
            }
    }.register()
}
