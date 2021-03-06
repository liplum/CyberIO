package net.liplum.welcome

import arc.Core
import arc.util.Log
import arc.util.Time
import mindustry.Vars
import net.liplum.CioMod
import net.liplum.R
import net.liplum.Settings
import net.liplum.lib.bundle
import net.liplum.ui.CioUI
import net.liplum.update.Updater

object Actions {
    val OpenLink = object : Action("OpenLink") {
        override fun doAction(entity: Welcome.Entity) {
            val data = entity.tip.data
            var link = data["Link"] as? String
            if (link != null) {
                link = if (link.startsWith('@'))
                    entity[link.substring(1)]
                else
                    link
                Core.app.openURI(link)
            }
        }
    }
    val CloseReceiveWelcome = object : Action("StopReceiveWelcome") {
        override fun doAction(entity: Welcome.Entity) {
            Settings.ShouldShowWelcome = false
        }
    }
    val StopCheckUpdate = object : Action("StopCheckUpdate") {
        override fun doAction(entity: Welcome.Entity) {
            Settings.ShowUpdate = false
        }
    }
    val UpdateCyberIO = object : Action("UpdateCyberIO") {
        override fun doAction(entity: Welcome.Entity) {
            if (CioMod.jarFile == null) {
                Updater.updateSelfByBuiltIn()
            } else {
                var progress = 0f
                val loading = Vars.ui.loadfrag
                loading.show("@downloading")
                loading.setProgress { progress }
                // Cache tips because updating successfully will replace codes and cause class not found exception.
                val successTip = R.Ctrl.UpdateModSuccess.bundle(Updater.latestVersion)
                Updater.updateSelfByReplace(Updater.DownloadURL, onProgress = { p ->
                    progress = p
                }, onSuccess = {
                    loading.hide()
                    Time.run(10f) {
                        Vars.ui.showInfoOnHidden(successTip) {
                            Core.app.exit()
                        }
                    }
                }, onFailed = { error ->
                    Log.err(error)
                    Core.app.post {
                        loading.hide()
                        CioUI.showUpdateFailed(error)
                    }
                })
            }
        }
    }
}