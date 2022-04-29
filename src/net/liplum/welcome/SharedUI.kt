package net.liplum.welcome

import arc.graphics.Texture
import arc.scene.ui.Dialog
import arc.scene.ui.Label
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
import arc.scene.utils.Elem
import arc.util.Scaling
import net.liplum.DesktopOnly
import net.liplum.utils.TR

internal fun Table.addPoster(icon: TR) {
    icon.texture.setFilter(Texture.TextureFilter.nearest)
    image(icon).minSize(200f).scaling(Scaling.fill).row()
}

internal fun Table.addCenterText(text: String) {
    add(Label(text).apply {
        setAlignment(0)
        setWrap(true)
        DesktopOnly {
            // On the high resolution screen, the text looks too small.
            setFontScale(1.1f)
        }
    }).growX()
        .row()
}

internal inline fun Table.addCloseButton(
    dialog: Dialog,
    text: String,
    crossinline task: () -> Unit = {},
): Cell<TextButton> {
    return button(text) {
        Welcome.recordClick()
        task()
        dialog.hide()
    }.size(200f, 50f)
}

internal inline fun Dialog.createCloseButton(
    text: String,
    crossinline task: () -> Unit = {},
): TextButton {
    return Elem.newButton(text) {
        Welcome.recordClick()
        task()
        hide()
    }
}
