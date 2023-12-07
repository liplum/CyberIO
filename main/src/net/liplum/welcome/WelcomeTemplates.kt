package net.liplum.welcome

import mindustry.ui.dialogs.BaseDialog
import net.liplum.R
import net.liplum.Var
import net.liplum.common.ui.RateStarPanelBuilder
import net.liplum.update.Updater
import net.liplum.welcome.Welcome.handleTrRefer
import plumy.core.arc.tinted


object WelcomeTemplates {
    class Story(
        val showPoliteWelcome: Boolean = true,
    ) : WelcomeTemplate() {
        override fun gen(entity: WelcomeEntity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                addPoster(entity.icon)
                if (showPoliteWelcome) addPoliteWelcome(entity)
                addCenterText(entity.content)
                addCloseButton(entity["read"])
            }
    }

    class ButtonABC(
        val actionA: WelcomeAction = WelcomeAction.Default,
        val actionB: WelcomeAction = WelcomeAction.Default,
        val actionC: WelcomeAction = WelcomeAction.Default,
        val showPoliteWelcome: Boolean = true,
    ) : WelcomeTemplate() {
        override fun gen(entity: WelcomeEntity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                addPoster(entity.icon)
                if (showPoliteWelcome) addPoliteWelcome(entity)
                cont.table {
                    addCloseButton(entity["button-a"], it, 200f) {
                        actionA(entity)
                    }
                    addCloseButton(entity["button-b"], it, 200f) {
                        actionB(entity)
                    }
                    addCloseButton(entity["button-c"], it, 200f) {
                        actionC(entity)
                    }
                }.growX()
                    .row()
            }
    }

    class DoAction(
        val yesAction: WelcomeAction = WelcomeAction.Default,
        val noAction: WelcomeAction = WelcomeAction.Default,
        val showPoliteWelcome: Boolean = true,
    ) : WelcomeTemplate() {
        override fun gen(entity: WelcomeEntity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                addPoster(entity.icon)
                if (showPoliteWelcome) addPoliteWelcome(entity)
                addCenterText(entity.content)
                cont.table {
                    addCloseButton(entity["yes"]) {
                        yesAction(entity)
                    }
                    addCloseButton(entity["no"]) {
                        noAction(entity)
                    }
                }.growX()
                    .row()
            }
    }

    class TextIcon(
        val text: String = "",
        val fontSize: Float = 1f,
        val showPoliteWelcome: Boolean = true,
    ) : WelcomeTemplate() {
        override fun gen(entity: WelcomeEntity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                val resText = entity.bundle.handleRefer(text)
                addCenterText(resText).get().apply {
                    this.setFontScale(fontSize)
                }
                if (showPoliteWelcome) addPoliteWelcome(entity)
                addCenterText(entity.content)
                addCloseButton(entity["read"])
            }
    }

    class PlainText(
        val fontSize: Float = 1f,
        val showPoliteWelcome: Boolean = true,
    ) : WelcomeTemplate() {
        override fun gen(entity: WelcomeEntity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                if (showPoliteWelcome) addPoliteWelcome(entity)
                addCenterText(entity.content).pad(40f).get().apply {
                    this.setFontScale(fontSize)
                }
                addCloseButton(entity["read"])
            }
    }

    class RateStar(
        val starSize: Float = 50f,
        val starNumber: Int = 5,
        val activeStarIconPath: String,
        val inactiveStarIconPath: String,
        val showPoliteWelcome: Boolean = true,
    ) : WelcomeTemplate() {
        override fun gen(entity: WelcomeEntity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                addPoster(entity.icon)
                if (showPoliteWelcome) addPoliteWelcome(entity)
                addCenterText(entity.content)
                val ratePanel = RateStarPanelBuilder(
                    starNumber = starNumber,
                    starSize = starSize,
                    activeStar = activeStarIconPath.handleTrRefer(),
                    inactiveStar = inactiveStarIconPath.handleTrRefer(),
                ).build()
                cont.add(ratePanel).row()
                addCloseButton(entity["submit"])
            }
    }

    object UpdateCyberIO : WelcomeTemplate() {
        override fun gen(entity: WelcomeEntity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                addPoster(entity.icon)
                addCenterText(entity.content(Updater.latestVersion.toString().tinted(Var.Hologram)))
                if (Updater.isCurrentBreakUpdate)
                    addCenterText(entity["break-update-warning"]).color(R.C.RedAlert)
                if (Updater.hasUpdateDescription)
                    addBoxedText(Updater.UpdateDescription)
                cont.table {
                    addCloseButton(entity["update"], it) {
                        WelcomeActions.UpdateCyberIO(entity)
                    }.size(150f, 50f)
                    addCloseButton(entity["no"], it) {
                    }.size(150f, 50f)
                    addCloseButton(entity["skip-this"], it) {
                        WelcomeActions.SkipThisUpdate(entity)
                    }.size(150f, 50f)
                }.growX()
                    .row()
            }
    }
}