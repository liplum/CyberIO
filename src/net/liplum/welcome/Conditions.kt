package net.liplum.welcome

import mindustry.Vars
import net.liplum.Settings
import net.liplum.update.Updater
import opengal.core.IExpressionReceiver
import opengal.experssion.ExpressionParser

object Conditions {
    val ShowWelcome = object : Condition("ShowWelcome") {
        override fun canShow(tip: WelcomeTip): Boolean {
            return Settings.ShouldShowWelcome
        }

        override fun priority(tip: WelcomeTip) = 0
    }
    val CheckUpdate = object : Condition("CheckUpdate") {
        override fun canShow(tip: WelcomeTip): Boolean {
            return !Vars.steam && Settings.ShowUpdate && Updater.requireUpdate
        }

        override fun priority(tip: WelcomeTip) = 10
    }
    val SpecialDishes = object : Condition("SpecialDishes") {
        override fun canShow(tip: WelcomeTip): Boolean {
            return Settings.ShouldShowWelcome
        }

        override fun priority(tip: WelcomeTip) =
            if (Settings.ClickWelcomeTimes == 0) 5 else 0
    }
    val SettingsReq = object : Condition("SettingsReq") {
        override fun canShow(tip: WelcomeTip): Boolean {
            val data = tip.data
            val exprRaw = data["CExpression"] as? String ?: ""
            val expr = ExpressionParser.by(exprRaw).parse<Boolean>()
            val res = expr.calculate(ExprSettingsWrapper)
            return res
        }

        override fun priority(tip: WelcomeTip) = 0
    }

    object ExprSettingsWrapper : IExpressionReceiver {
        override fun set(name: String, value: Any) =
            throw NotImplementedError("Can't set $name as $value")

        override fun <T : Any> get(name: String): T =
            Settings[name]
    }
}