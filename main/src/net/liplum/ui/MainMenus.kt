package net.liplum.ui

import arc.Core
import arc.Events
import arc.scene.style.Drawable
import arc.scene.ui.ScrollPane
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Table
import arc.scene.ui.layout.WidgetGroup
import mindustry.Vars
import mindustry.game.EventType
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.ui.MobileButton
import mindustry.ui.Styles
import net.liplum.ui.template.NewIconTextButton
import plumy.dsl.bundle

object MainMenus {
    private var counter = 0
    @JvmStatic
    fun appendMobileMenu(
        text: String,
        icon: Drawable,
        id: String = "custom-menu-${counter++}",
        onClicked: () -> Unit,
    ) {
        if (!Vars.mobile) return
        val menuGroup = Vars.ui.menuGroup
        val buttons = menuGroup.find<Table>("buttons")
        fun rebuild() {
            val needRow = buttons.children.count {
                it is MobileButton && it.name != id
            } % (if (Core.graphics.isPortrait) 2 else 4) == 0
            if (needRow) buttons.row()
            buttons.add(MobileButton(icon, text, onClicked).apply {
                name = id
            })
        }
        rebuild()
        Events.on(EventType.ResizeEvent::class.java) {
            rebuild()
        }
    }
    @JvmStatic
    fun appendDesktopMenu(
        text: String,
        icon: Drawable,
        id: String = "custom-menu-${counter++}",
        onClicked: () -> Unit,
    ) {
        if (Vars.mobile) return
        val menuGroup = Vars.ui.menuGroup
        val parent = menuGroup.children[0] as WidgetGroup
        fun rebuild() {
            // To solve `the length of cells is 1 in Mindustry v139 ` https://github.com/liplum/CyberIO/issues/37
            val table1 = parent.children[1] as Table
            val table1Cells = table1.cells
            val cellIndex = 1.coerceAtMost(table1Cells.size - 1)
            val menuButtonContainer = table1Cells[cellIndex].get()
            val buttons = if (menuButtonContainer is ScrollPane)
                (menuButtonContainer.children[0] as Table).cells[1].get() as Table
            else menuButtonContainer as Table
            val button = NewIconTextButton(
                text, icon,
                Vars.iconMed, TextButton.TextButtonStyle(Styles.cleart).apply {
                    up = Tex.clear
                    down = Styles.flatDown
                }, onClicked
            ).apply {
                name = id
            }
            // Remove the quit button and add it back after this custom menu soon
            val exit = buttons.find<TextButton> {
                it is TextButton && it.text.toString() == "quit".bundle
            }
            if (exit != null) {
                buttons.removeChild(exit)
                buttons.cells.retainAll { it.hasElement() }
            }
            buttons.add(button).marginLeft(11f).row()
            buttons.button("@quit", Icon.exit, Styles.flatToggleMenut, Core.app::exit)
                .marginLeft(11f).row()
        }
        rebuild()
        Events.on(EventType.ResizeEvent::class.java) {
            rebuild()
        }
    }
}