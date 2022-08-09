package net.liplum.ui

import arc.Core
import arc.scene.Element
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.Cell

fun <T : ScrollPane> T.autoLoseFocus(): T {
    update {
        if (this.hasScroll()) {
            val result: Element? =
                Core.scene.hit(Core.input.mouseX().toFloat(), Core.input.mouseY().toFloat(), true)
            if (result == null || !result.isDescendantOf(this)) {
                Core.scene.scrollFocus = null
            }
        }
    }
    return this
}

fun <T : Element> Cell<T>.autoLoseFocus(): Cell<T> {
    update {
        if (it.hasScroll()) {
            val result: Element? =
                Core.scene.hit(Core.input.mouseX().toFloat(), Core.input.mouseY().toFloat(), true)
            if (result == null || !result.isDescendantOf(it)) {
                Core.scene.scrollFocus = null
            }
        }
    }
    return this
}
@Deprecated("Unverified API", level = DeprecationLevel.WARNING)
fun <T : Element> T.track(target: Element): T {
    update {
        this.setPosition(target.x, target.y)
    }
    return this
}
