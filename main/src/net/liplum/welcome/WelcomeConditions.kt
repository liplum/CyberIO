package net.liplum.welcome

import mindustry.Vars
import net.liplum.Settings
import net.liplum.update.Updater

object WelcomeConditions {
    object ShowWelcome : WelcomeCondition() {
        override fun canShow(tip: WelcomeScene): Boolean {
            return Settings.ShouldShowWelcome
        }

        override fun priority(tip: WelcomeScene) = 0
    }

    object CheckUpdate : WelcomeCondition() {
        override fun canShow(tip: WelcomeScene): Boolean {
            return !Vars.steam && Settings.ShowUpdate &&
                    Updater.requireUpdate &&
                    !Updater.latestVersion.equalsString(Settings.LastSkippedUpdate) &&
                    Updater.matchMinGameVersion
        }

        override fun priority(tip: WelcomeScene) = 10
    }

    object SpecialDishes : WelcomeCondition() {
        override fun canShow(tip: WelcomeScene): Boolean {
            return Settings.ShouldShowWelcome
        }

        override fun priority(tip: WelcomeScene) =
            if (Settings.ClickWelcomeTimes == 0) 5 else 0
    }

    class Expr(
        val priority: Int = 0,
        val expr: () -> Boolean,
    ) : WelcomeCondition() {
        override fun canShow(tip: WelcomeScene): Boolean {
            return expr()
        }

        override fun priority(tip: WelcomeScene) = priority
    }
}