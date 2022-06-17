package net.liplum.mdt.ui

import arc.Core
import arc.Events
import arc.scene.style.Drawable
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Table
import arc.scene.ui.layout.WidgetGroup
import arc.util.Time
import mindustry.Vars
import mindustry.game.EventType
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.ui.MobileButton
import mindustry.ui.Styles
import net.liplum.common.utils.bundle
import net.liplum.ui.templates.NewIconTextButton

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
        Time.run(7f) {
            rebuild()
            Events.on(EventType.ResizeEvent::class.java) {
                rebuild()
            }
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
            val buttons = (parent.children[1] as Table).cells[1].get() as Table
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
                buttons.cells.filter { it.hasElement() }
            }
            buttons.add(button).marginLeft(11f).row()
            buttons.button("@quit", Icon.exit, Styles.flatToggleMenut, Core.app::exit)
                .marginLeft(11f)
        }

        fun coroutine() {
            if (Core.assets.progress != 1f) {
                Core.app.post {
                    coroutine()
                }
                return
            }
            Time.run(7f) {
                rebuild()
                Events.on(EventType.ResizeEvent::class.java) {
                    rebuild()
                }
            }
        }
        coroutine()
    }
}