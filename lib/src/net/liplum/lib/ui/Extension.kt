package net.liplum.lib.ui

import arc.Core
import arc.graphics.Color
import arc.scene.Element
import arc.scene.event.InputEvent
import arc.scene.event.InputListener
import arc.scene.event.VisibilityListener
import arc.scene.style.Drawable
import arc.scene.ui.Label
import arc.scene.ui.ScrollPane
import arc.scene.ui.Tooltip
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
import arc.util.Align
import arc.util.Tmp
import kotlin.properties.ReadOnlyProperty

inline fun <T : Element> Cell<T>.then(func: T.() -> Unit): Cell<T> {
    this.get().func()
    return this
}

inline fun <T : Element> Cell<T>.and(func: T.() -> Unit): T {
    val element = this.get()
    element.func()
    return element
}

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

fun <T : ScrollPane> Cell<T>.autoLoseFocus(): Cell<T> {
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

fun <T : Element> T.addEasyTooltip(
    text: String,
    background: Drawable,
): T {
    addListener(Tooltip {
        it.background(background).margin(4f).add(text)
    })
    return this
}

inline fun <T : Element> T.addTooltip(
    background: Drawable,
    crossinline ctor: T.(Table) -> Unit,
): T {
    addListener(Tooltip {
        this.ctor(it)
        it.background(background).margin(4f)
    })
    return this
}

fun <T : Element> T.addTrackTooltip(text: String, background: Drawable): T {
    addListener(object : Tooltip({
        it.background(background).margin(4f)
        it.add(Label(text).apply {
            setColor(Color.lightGray)
        }).growX()
    }) {
        override fun setContainerPosition(element: Element, x: Float, y: Float) {
            targetActor = element
            val pos = element.localToStageCoordinates(Tmp.v1.set(0f, 0f))
            container.pack()
            container.setPosition(pos.x, pos.y, Align.topLeft)
            container.setOrigin(0f, element.height)
        }
    }.apply {
        allowMobile = true
    })
    return this
}

fun <T : Element> T.dragToMove(): T {
    addListener(Dragger(this))
    return this
}
/**
 * Unverified API
 */
fun <T : Element> T.track(target: Element): T {
    update {
        this.setPosition(target.x, target.y)
    }
    return this
}

fun <T : Element> T.isMouseOver(): ReadOnlyProperty<Any?, Boolean> {
    var isMouseOver = false
    addListener(object : InputListener() {
        override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Element?) {
            isMouseOver = true
        }

        override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Element?) {
            isMouseOver = false
        }
    })
    return ReadOnlyProperty { _, _ -> isMouseOver }
}

inline fun <T : Element> T.onHidden(crossinline func: T.() -> Unit) {
    addListener(object : VisibilityListener() {
        override fun hidden(): Boolean {
            func()
            return false
        }
    })
}

fun Table.addSeparatorLine(
    color: Color = Color.white,
) {
    this.image().growX().pad(5f).padLeft(0f).padRight(0f).height(3f).color(color).row()
}