package net.liplum.mdt.ui.tabview

import arc.graphics.Color
import arc.scene.ui.Button
import arc.scene.ui.ButtonGroup
import arc.scene.ui.layout.Table
import arc.util.Align
import mindustry.gen.Tex
import mindustry.ui.Styles
import net.liplum.lib.ui.*

class TabView : INavigable {
    var items = LinkedHashMap<String, TabItem>()
    var navigationService: () -> INavigationService? = { null }
    var xName = ""
    var rememberBuilt = false
    private var item2Built = HashMap<TabItem, Built>()
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
        if (curItem == TabItem.X && defaultItem != TabItem.X)
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
                            if (isChecked)
                                color.set(Color.white)
                            else
                                color.set(Color.gray).a(0.4f)
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

    fun resetItem() {
        switchTo(TabItem.X)
    }

    fun switchTo(id: String): INavigable? {
        val item = items[id]
        if (item != null) {
            return switchTo(item)
        }
        return null
    }

    fun switchTo(item: TabItem): INavigable? {
        if (curItem != item) {
            curItem = item
            if (rememberBuilt) {
                val built = item2Built[item]
                return if (built == null) {
                    curContent.clear()
                    val container = Table()
                    val navigable = item.buildContent(container)
                    item2Built[item] = Built(container, navigable)
                    curContent.add(container).grow()
                    navigable
                } else {
                    curContent.clear()
                    curContent.add(built.content).grow()
                    built.navigable
                }
            } else {// it not remembers built
                curContent.clear()
                val container = Table()
                val navigable = item.buildContent(container)
                curContent.add(container).grow()
                return navigable
            }
        }
        return null
    }

    override val navigateFragment: String
        get() = xName
    private val sharedLocator = Navigator()
    override fun navigate(locator: INavigator): Boolean {
        val navigation = navigationService()
        if (navigation != null && locator.kind == NavigateKind.Global) {
            return navigation.navigate(locator)
        } else {
            sharedLocator.copyFrom(locator)
            val frags = sharedLocator.fragments
            if (frags.isNotEmpty()) {
                val id = frags.removeFirst()
                val navigable = switchTo(id)
                return navigable?.navigate(sharedLocator) ?: false
            }
        }
        return false
    }

    class Built(
        val content: Table,
        val navigable: INavigable,
    )
}

open class TabItem(
    /** Used for navigation  */
    val id: String,
) {
    override fun toString() = id
    var buildIcon: Table.() -> Unit = {}
    var buildContent: Table.() -> INavigable = { EmptyNavigable }
    inline fun buildIcon(crossinline func: Table.() -> Unit) {
        buildIcon = {
            func()
        }
    }

    inline fun buildContent(crossinline func: Table.() -> Any) {
        buildContent = {
            val res = func()
            if (res is INavigable) res
            else EmptyNavigable
        }
    }

    companion object {
        val X = TabItem("")
    }
}
