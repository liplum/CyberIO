package net.liplum.welcome

import arc.Core
import net.liplum.CLog
import net.liplum.Settings
import net.liplum.Var
import net.liplum.ui.Navigator
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
            Updater.Release.update()
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
                    CLog.err("In action [$id]", e)
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