package net.liplum.welcome

import mindustry.Vars
import net.liplum.Settings
import net.liplum.update.Updater
import net.liplum.utils.randomExcept

object Conditions {
    val ShowWelcome = object : Condition("ShowWelcome", 0) {
        override fun canShow(tip:WelcomeTip): Boolean {
            return Settings.ShouldShowWelcome
        }
        /**
         * If it's the first time to play this version, let's show up the Zero Welcome.
         *
         * Otherwise, roll until the result isn't as last one.
         */
        override fun applyShow(entity: Welcome.Entity, matches: List<WelcomeTip>) {
            val info = entity.info
            val defaultTipID = info.default
            if (Settings.ClickWelcomeTimes > 0) {
                val lastWelcomeID = Settings.LastWelcomeID
                val curTip = matches.randomExcept {
                    id == lastWelcomeID
                }
                if (curTip != null) {
                    val tip = curTip
                    entity.tip = tip
                    Settings.LastWelcomeID = curTip.id
                } else {
                    entity.tip = WelcomeList[defaultTipID]
                    Settings.LastWelcomeID = defaultTipID
                }
            } else {
                entity.tip = WelcomeList[defaultTipID]
                Settings.LastWelcomeID = defaultTipID
            }
        }
    }
    val CheckUpdate = object : Condition("CheckUpdate", 10) {
        override fun canShow(tip:WelcomeTip): Boolean {
            return !Vars.steam && Settings.ShowUpdate && Updater.requireUpdate
        }

        override fun applyShow(entity: Welcome.Entity, matches: List<WelcomeTip>) {
            entity.tip = matches.random()
        }
    }
}