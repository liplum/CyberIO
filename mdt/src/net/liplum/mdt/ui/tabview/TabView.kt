package net.liplum.mdt.ui.tabview

import arc.scene.ui.Button
import arc.scene.ui.ButtonGroup
import arc.scene.ui.layout.Table
import arc.util.Align
import mindustry.gen.Tex
import mindustry.ui.Styles
import java.util.*

class TabView {
    var items: MutableCollection<TabItem> = LinkedList()
    var default = TabItem.X
    var curContent = Table(Tex.button)
    fun addTab(item: TabItem): TabView {
        if (item != TabItem.X) {
            if (default == TabItem.X)
                default = item
            items.add(item)
        }
        return this
    }

    fun build(table: Table) {
        curContent.clear() // If this is reused.
        table.defaults().fill().left()
        table.add(Table().apply {
            val group = ButtonGroup<Button>()
            val tabMenu = Table().apply {
                for (item in items) {
                    add(Button(Styles.flatToggleMenut).apply {
                        item.tabIcon(this)
                        group.add(this)
                        changed {
                            curContent.clear()
                            item.tabContent(curContent)
                        }
                    }).margin(10f).pad(5f).grow()
                }
            }
            add(tabMenu)
            align(Align.left)
        }).minHeight(30f)
        table.row()
        table.add(Table().apply {
            add(curContent).grow()
        }).grow()
        default.tabContent(curContent)
    }
}

class TabItem {
    var tabIcon: Table.() -> Unit = {}
    var tabContent: Table.() -> Unit = {}

    companion object {
        val X = TabItem()
    }
}