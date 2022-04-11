package net.liplum

import arc.Core.settings
import arc.Events
import arc.scene.ui.Label
import arc.util.Time
import mindustry.game.EventType.Trigger
import mindustry.ui.dialogs.BaseDialog
import net.liplum.R.Setting
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.utils.inCio
import net.liplum.utils.set

object Welcome {
    @JvmStatic
    fun showWelcomeDialog() {
        checkLastVersion()
        if (shouldShowWelcome) {
            dialog.show()
        }
    }

    @JvmStatic
    fun checkLastVersion() {
        val lastVersion = settings.getString(Setting.Version, Meta.Version)
        if (lastVersion != Meta.Version) {
            settings.set(Setting.ShowWelcome, true)
            settings.set(Setting.Version, Meta.Version)
        }
    }

    @JvmStatic
    fun recordClick() {
        val formerTimes = settings.getInt(Setting.ClickWelcomeTimes, 0)
        settings.set(Setting.ClickWelcomeTimes, formerTimes + 1)
    }

    val shouldShowWelcome: Boolean
        get() {
            val showWelcome = settings.getBool(Setting.ShowWelcome, true)
            return showWelcome
        }
    val dialog = BaseDialog(News.getTitle()).apply {
        cont.image("icon".inCio())
            .maxSize(200f).pad(20f).row()
        val welcomeLabel = Label(News.getWelcome()).apply {
            setAlignment(0)
            setWrap(true)
        }
        cont.add(welcomeLabel)
            .growX()
            .row()
        val newsLabel = Label(News.getNews()).apply {
            setAlignment(0)
            setWrap(true)
        }
        cont.add(newsLabel)
            .growX()
            .row()
        cont.button(News.getRead()) {
            recordClick()
            hide()
        }.size(100f, 50f)
        layout()
    }

    fun modifierModInfo() {
        val meta = CioMod.Info.meta
        meta.displayName = "[#${R.C.Holo}]${meta.displayName}[]"
        Events.run(Trigger.update) {
            if (Time.time % 30 < 1f) {
                val color = RandomName.oneColor()
                meta.author = "$color${Meta.Author}[]"
            }
        }
    }
}