package net.liplum.welcome

import arc.Core
import arc.util.Log
import arc.util.Time
import mindustry.Vars
import net.liplum.*
import net.liplum.ui.Navigator
import net.liplum.common.util.bundle
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
    val SkipThisUpdate = object : Action("SkipThisUpdate") {
        override fun doAction(entity: Welcome.Entity) {
            val latest = Updater.latestVersion
            Settings.LastSkippedUpdate = latest.toString()
        }
    }
    val UpdateCyberIO = object : Action("UpdateCyberIO") {
        override fun doAction(entity: Welcome.Entity) {
            if (CioMod.jarFile == null) {
                Updater.Android.updateSelfByBuiltIn()
            } else {
                var progress = 0f
                val loading = Vars.ui.loadfrag
                loading.show("@downloading")
                loading.setProgress { progress }
                // Cache tips because updating successfully will replace codes and cause class not found exception.
                val successTip = R.Ctrl.UpdateModSuccess.bundle(Updater.latestVersion)
                Updater.Desktop.updateSelfByReplace(Updater.Desktop.curDownloadURL, onProgress = { p ->
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
    val CallStaticFunction = object : Action("CallStaticFunction") {
        override fun doAction(entity: Welcome.Entity) {
            val data = entity.tip.data
            val clzName = data["ClassFullName"] as? String
            val funcName = data["StaticFunctionName"] as? String
            if (clzName != null && funcName != null) {
                try {
                    val clz = Class.forName(clzName)
                    val method = clz.getMethod(funcName)
                    method.invoke(null)
                } catch (e: Exception) {
                    CLog.err("In action [$id]",e)
                }
            }
        }
    }
    val Navigation = object : Action("Navigation") {
        override fun doAction(entity: Welcome.Entity) {
            val data = entity.tip.data
            val locatorText = data["Locator"] as? String
            if (locatorText != null) {
                val locator = Navigator.by(locatorText)
                Var.Navigation.navigate(locator)
            }
        }
    }
}