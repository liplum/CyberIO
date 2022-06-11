package net.liplum.mdt.ui.tabview

import arc.scene.ui.Button
import arc.scene.ui.ButtonGroup
import arc.scene.ui.layout.Table
import arc.util.Align
import mindustry.gen.Tex
import mindustry.ui.Styles
import net.liplum.lib.ui.INavigable
import net.liplum.lib.ui.INavigationService
import net.liplum.lib.ui.INavigator
import net.liplum.lib.ui.NavigateKind

class TabView : INavigable {
    var items = LinkedHashMap<String, TabItem>()
    var navigationService: () -> INavigationService? = { null }
    var xName = ""
    var rememberBuilt = false
    private var item2Built = HashMap<TabItem, Table>()
    var curContent = Table(Tex.button)
        private set
    var curItem = TabItem.X
        private set
    var defaultItem = TabItem.X

    fun addTab(item: TabItem): TabView {
        if (item != TabItem.X) {
            if (defaultItem == TabItem.X)
                defaultItem = item
            items[item.id] = item
        }
        return this
    }

    fun build(table: Table) {
        if (defaultItem != TabItem.X)
            switchTo(defaultItem)
        table.defaults().fill().left()
        table.add(Table().apply {
            val group = ButtonGroup<Button>()
            val tabMenu = Table().apply {
                for (item in items.values) {
                    add(Button(Styles.flatToggleMenut).apply {
                        item.buildIcon(this)
                        group.add(this)
                        changed {
                            switchTo(item)
                        }
                        update {
                            isChecked = curItem == item
                        }
                    }).margin(10f).pad(5f).grow()
                }
            }
            add(tabMenu)
            align(Align.left)
        }).minHeight(30f)
        table.row()
        table.add(curContent).grow()
    }

    fun switchTo(id: String) {
        val item = items[id]
        if (item != null) {
            switchTo(item)
        }
    }

    fun switchTo(item: TabItem) {
        if (curItem != item) {
            curItem = item
            if (rememberBuilt) {
                val built = item2Built[item]
                if (built == null) {
                    curContent.clear()
                    val container = Table()
                    item.buildContent(container)
                    item2Built[item] = container
                    curContent.add(container).grow()
                } else {
                    curContent.clear()
                    curContent.add(built).grow()
                }
            } else {// it not remembers built
                curContent.clear()
                val container = Table()
                item.buildContent(container)
                curContent.add(container).grow()
            }
        }
    }

    override val navigateFragment: String
        get() = xName

    override fun navigate(locator: INavigator): Boolean {
        val navigation = navigationService()
        if (navigation != null && locator.kind == NavigateKind.Global) {
            return navigation.navigate(locator)
        } else {
            val id = locator.fragments.firstOrNull()
            if (id != null) {
                switchTo(id)
                return true
            }
        }
        return false
    }
}

class TabItem(
    /** Used for navigation  */
    val id: String,
) {
    var buildIcon: Table.() -> Unit = {}
    var buildContent: Table.() -> Unit = {}

    companion object {
        val X = TabItem("")
    }
}