package net.liplum.ui

import mindustry.ui.dialogs.BaseDialog
import net.liplum.mdt.ui.tabview.TabItem
import net.liplum.mdt.ui.tabview.TabView

object CyberIODialog {
    fun show() {
        BaseDialog("Cyber IO").apply {
            allTabs.build(cont)
            addCloseListener()
        }.show()
    }

    var allTabs = TabView().apply {
        // Select the Cyber IO specific
        addTab(TabItem().apply {
            tabIcon = {
                add(ContentSpecFrag.title)
            }
            tabContent = {
                ContentSpecFrag.build(this)
            }
        })
    }
}