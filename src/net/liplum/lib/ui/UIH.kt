package net.liplum.lib.ui

import arc.graphics.Color
import arc.scene.Element
import arc.scene.ui.Label
import arc.scene.ui.Tooltip
import arc.scene.ui.layout.Table
import arc.util.Align
import arc.util.Tmp
import mindustry.ui.Styles
import mindustry.ui.dialogs.BaseDialog

inline fun ShowTextDialog(
    crossinline text: () -> String = { "" },
): BaseDialog {
    return BaseDialog("").apply {
        cont.label { text() }
        addCloseButton()
        show()
    }
}

fun ShowTextDialog(
    text: String,
): BaseDialog {
    return BaseDialog("").apply {
        cont.add(text)
        addCloseButton()
        show()
    }
}

fun <T : Element> T.addTooltip(text: String): T {
    addListener(Tooltip {
        it.background(Styles.black8).margin(4f).add(text)
    })
    return this
}

inline fun <T : Element> T.addTooltip(
    crossinline ctor: T.(Table) -> Unit,
): T {
    addListener(Tooltip {
        this.ctor(it)
        it.background(Styles.black8).margin(4f)
    })
    return this
}

fun <T : Element> T.addTrackTooltip(text: String): T {
    addListener(object : Tooltip({
        it.background(Styles.black8).margin(4f)
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
