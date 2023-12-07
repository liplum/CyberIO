package net.liplum.welcome

import arc.Core
import net.liplum.CLog
import net.liplum.Settings
import net.liplum.Var
import net.liplum.ui.Navigator
import net.liplum.update.Updater

object WelcomeActions {
    class OpenLink(
        val link: String,
    ) : WelcomeAction() {
        override fun doAction(entity: WelcomeEntity) {
            var link = this.link
            link = if (link.startsWith('@'))
                entity[link.substring(1)]
            else
                link
            Core.app.openURI(link)
        }
    }

    object StopReceiveWelcome : WelcomeAction() {
        override fun doAction(entity: WelcomeEntity) {
            Settings.ShouldShowWelcome = false
        }
    }

    object StopCheckUpdate : WelcomeAction() {
        override fun doAction(entity: WelcomeEntity) {
            Settings.ShowUpdate = false
        }
    }

    object SkipThisUpdate : WelcomeAction() {
        override fun doAction(entity: WelcomeEntity) {
            val latest = Updater.latestVersion
            Settings.LastSkippedUpdate = latest.toString()
        }
    }

    object UpdateCyberIO : WelcomeAction() {
        override fun doAction(entity: WelcomeEntity) {
            Updater.Release.update()
        }
    }

    class CallStaticFunction(
        val classFullName: String,
        val funcName: String,
    ) : WelcomeAction() {
        override fun doAction(entity: WelcomeEntity) {
            try {
                val clz = Class.forName(classFullName)
                val method = clz.getMethod(funcName)
                method.invoke(null)
            } catch (e: Exception) {
                CLog.err("Failed to call ${classFullName}.${funcName}", e)
            }
        }
    }

    class Navigation(
        val destination: String,
    ) : WelcomeAction() {
        override fun doAction(entity: WelcomeEntity) {
            val locator = Navigator.by(destination)
            Var.Navigation.navigate(locator)
        }
    }
}