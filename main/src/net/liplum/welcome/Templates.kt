package net.liplum.welcome

import arc.scene.ui.Button
import mindustry.ui.dialogs.BaseDialog
import net.liplum.R
import net.liplum.S
import net.liplum.lib.utils.tinted
import net.liplum.lib.ui.RateStarPanelBuilder
import net.liplum.update.Updater
import net.liplum.welcome.Welcome.Entity
import net.liplum.welcome.Welcome.handleTrRefer

object Templates {
    val Story = object : WelcomeTemplate("Story") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                val data = entity.tip.data
                addPoster(entity.icon)
                val showPoliteWelcome = data["ShowPoliteWelcome"] as? Boolean ?: true
                if (showPoliteWelcome) addPoliteWelcome(entity)
                addCenterText(entity.content)
                addCloseButton(entity["read"])
            }
    }
    val ButtonABC = object : WelcomeTemplate("ButtonABC") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                val data = entity.tip.data
                val yesAction = ActionRegistry[data["ActionA"]]
                val noAction = ActionRegistry[data["ActionB"]]
                val dontShowAction = ActionRegistry[data["ActionC"]]
                val showPoliteWelcome = data["ShowPoliteWelcome"] as? Boolean ?: true
                addPoster(entity.icon)
                if (showPoliteWelcome) addPoliteWelcome(entity)
                cont.table {
                    addCloseButton(entity["button-a"], it) {
                        yesAction(entity)
                    }.size(150f, 50f)
                    addCloseButton(entity["button-b"], it) {
                        noAction(entity)
                    }.size(150f, 50f)
                    addCloseButton(entity["button-c"], it) {
                        dontShowAction(entity)
                    }.size(150f, 50f)
                }.growX()
                    .row()
            }
    }
    val DoAction = object : WelcomeTemplate("DoAction") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                val data = entity.tip.data
                addPoster(entity.icon)
                val showPoliteWelcome = data["ShowPoliteWelcome"] as? Boolean ?: false
                if (showPoliteWelcome) addPoliteWelcome(entity)
                val yesAction = ActionRegistry[data["YesAction"]]
                val noAction = ActionRegistry[data["NoAction"]]
                addCenterText(entity.content)
                cont.table {
                    fun addButton(vararg buttons: Button) {
                        for (b in buttons)
                            it.add(b).size(200f, 50f)
                    }

                    val yes = createCloseButton(entity["yes"]) {
                        yesAction(entity)
                    }
                    val no = createCloseButton(entity["no"]) {
                        noAction(entity)
                    }
                    addButton(yes, no)
                }.growX()
                    .row()
            }
    }
    val TextIcon = object : WelcomeTemplate("TextIcon") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                val data = entity.tip.data
                val text = data["Text"] as? String ?: ""
                val fontSize = data["FontSize"] as? Float ?: 1f
                val resText = entity.bundle.handleRefer(text)
                addCenterText(resText).get().apply {
                    this.setFontScale(fontSize)
                }
                val showPoliteWelcome = data["ShowPoliteWelcome"] as? Boolean ?: true
                if (showPoliteWelcome) addPoliteWelcome(entity)
                addCenterText(entity.content)
                addCloseButton(entity["read"])
            }
    }
    val PlainText = object : WelcomeTemplate("PlainText") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                val data = entity.tip.data
                val showPoliteWelcome = data["ShowPoliteWelcome"] as? Boolean ?: true
                if (showPoliteWelcome) addPoliteWelcome(entity)
                val fontSize = data["FontSize"] as? Float ?: 1f
                addCenterText(entity.content).pad(40f).get().apply {
                    this.setFontScale(fontSize)
                }
                addCloseButton(entity["read"])
            }
    }
    val rateStar = object : WelcomeTemplate("RateStar") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                val data = entity.tip.data
                addPoster(entity.icon)
                val showPoliteWelcome = data["ShowPoliteWelcome"] as? Boolean ?: true
                if (showPoliteWelcome) addPoliteWelcome(entity)
                addCenterText(entity.content)
                val starSize = data["StarSize"] as? Float ?: 50f
                val ratePanel = RateStarPanelBuilder().apply {
                    starNumber = 5
                    this.starSize = starSize
                    (data["InactiveStarIconPath"] as? String)?.let { inactiveStar = it.handleTrRefer() }
                    (data["ActiveStarIconPath"] as? String)?.let { activeStar = it.handleTrRefer() }
                }.build()
                cont.add(ratePanel).row()
                addCloseButton(entity["submit"])
            }
    }
    val Update = object : WelcomeTemplate("Update") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                addCloseListener()
                val data = entity.tip.data
                val yesAction = ActionRegistry[data["ActionA"]]
                val noAction = ActionRegistry[data["ActionB"]]
                val dontShowAction = ActionRegistry[data["ActionC"]]
                addPoster(entity.icon)
                addCenterText(entity.content(Updater.latestVersion.toString().tinted(S.Hologram)))
                if (Updater.isCurrentBreakUpdate)
                    addCenterText(entity["break-update-warning"].tinted(R.C.RedAlert))
                if (Updater.hasUpdateDescription)
                    addBoxedText(Updater.UpdateDescription)
                cont.table {
                    addCloseButton(entity["button-a"], it) {
                        yesAction(entity)
                    }.size(150f, 50f)
                    addCloseButton(entity["button-b"], it) {
                        noAction(entity)
                    }.size(150f, 50f)
                    addCloseButton(entity["button-c"], it) {
                        dontShowAction(entity)
                    }.size(150f, 50f)
                }.growX()
                    .row()
            }
    }
}