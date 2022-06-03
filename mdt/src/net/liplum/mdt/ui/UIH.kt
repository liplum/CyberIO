package net.liplum.mdt.ui

import arc.scene.Element
import arc.scene.ui.Label
import arc.scene.ui.layout.Table
import mindustry.ui.Styles
import mindustry.ui.dialogs.BaseDialog
import net.liplum.lib.ui.addTooltip
import net.liplum.lib.ui.addTrackTooltip

val NewBaseDialog: BaseDialog
    get() = BaseDialog("")

inline fun ShowTextDialog(
    crossinline text: () -> String = { "" },
): BaseDialog {
    return BaseDialog("").apply {
        cont.add(Label { text() }.apply {
            setWrap(true)
            setAlignment(0)
        }).growX()
        addCloseButton()
        show()
    }
}

fun ShowTextDialog(
    text: String,
): BaseDialog {
    return BaseDialog("").apply {
        cont.add(Label(text).apply {
            setWrap(true)
            setAlignment(0)
        }).growX()
        addCloseButton()
        show()
    }
}

fun <T : Element> T.addTooltip(text: String): T =
    addTooltip(text, Styles.black8)

inline fun <T : Element> T.addTooltip(
    crossinline ctor: T.(Table) -> Unit,
): T = addTooltip(Styles.black8, ctor)

fun <T : Element> T.addTrackTooltip(text: String): T =
    addTrackTooltip(text, Styles.black8)