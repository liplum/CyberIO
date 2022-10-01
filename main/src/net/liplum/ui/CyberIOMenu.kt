package net.liplum.ui

import arc.graphics.Color
import mindustry.gen.Tex
import mindustry.ui.Styles
import mindustry.ui.dialogs.BaseDialog
import net.liplum.DebugOnly
import net.liplum.Var
import net.liplum.annotations.Only
import net.liplum.annotations.SubscribeEvent
import net.liplum.event.CioInitEvent
import net.liplum.heimdall.HeimdallProjectGame
import plumy.core.Else
import net.liplum.ui.control.TabItem
import net.liplum.ui.control.TabView
import net.liplum.ui.control.TabViewStyle

object CyberIOMenu : INavigable {
    val tabViewStyle = TabViewStyle().apply {
        contentViewStyle = Tex.whitePane
        contentViewStyler {
            setColor(Color.gray)
        }
        tabOptionStyle = Styles.flatTogglet
    }

    fun show() {
        BaseDialog("Cyber IO").apply {
            allTabs.build(cont)
            addCloseListener()
        }.show()
    }

    var allTabs = TabView(tabViewStyle).apply {
        // Select the Cyber IO specific
        addTab(TabItem("ContentSpecific").apply {
            buildIcon {
                add(ContentSpecFrag.title)
                    .tooltip(ContentSpecFrag.bundle("button-tip"))
            }
            buildContent {
                ContentSpecFrag.build(this)
            }
        })
        DebugOnly {
            addTab(TabItem("Heimdall").apply {
                buildIcon {
                    add(HeimdallProjectGame.title)
                        .tooltip(HeimdallProjectGame.bundle("button-tip"))
                }
                buildContent {
                    HeimdallProjectGame.build(this)
                }
            })
        }
        navigationService = { Var.Navigation }
        DebugOnly {
            rememberBuilt = false
        }.Else {
            rememberBuilt = true
        }
    }
    override val navigateFragment = "CyberIO"
    private val sharedLocator = Navigator()
    override fun navigate(locator: INavigator): Boolean {
        if (locator.kind == NavigateKind.Global) {
            return Var.Navigation.navigate(locator)
        } else {
            val frags = locator.fragments
            if (frags.isNotEmpty()) {
                sharedLocator.copyFrom(locator)
                if (allTabs.navigate(sharedLocator)) {
                    show()
                    return true
                }
            }
        }
        return false
    }
    @SubscribeEvent(CioInitEvent::class, Only.client)
    fun registerToGlobalNavigation() {
        Var.Navigation.registerNavigable(this)
    }
}