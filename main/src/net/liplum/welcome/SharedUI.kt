package net.liplum.welcome

import arc.graphics.Texture.TextureFilter.nearest
import arc.scene.ui.Dialog
import arc.scene.ui.Image
import arc.scene.ui.Label
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
import arc.scene.utils.Elem
import arc.util.Scaling
import net.liplum.CioMod
import net.liplum.Meta
import net.liplum.i18nName
import net.liplum.lib.TR
import net.liplum.mdt.DesktopOnly

internal fun Dialog.addPoster(
    icon: TR,
    table: Table = this.cont
): Image {
    val tx = icon.texture
    val magFilter = tx.magFilter
    val minFilter = tx.minFilter
    val img = Image(icon)
    shown {
        tx.setFilter(nearest)
    }
    hidden {
        if (magFilter != tx.magFilter || minFilter != tx.minFilter)
            tx.setFilter(magFilter, minFilter)
    }
    table.add(img).minSize(200f).scaling(Scaling.fill).row()
    return img
}

internal fun Dialog.addPoliteWelcome(entity: Welcome.Entity) {
    addCenterText(
        entity.bundle.format(
            "welcome",
            "${Meta.DetailedVersion} ${CioMod.ContentSpecific.i18nName}"
        )
    )
}

internal fun Dialog.addCenterText(
    text: String,
    table: Table = this.cont
): Cell<Label> {
    val cell = table.add(Label(text).apply {
        setAlignment(0)
        setWrap(true)
        DesktopOnly {
            // On the high resolution screen, the text looks too small.
            setFontScale(1.1f)
        }
    })
    cell.growX().row()
    return cell
}

internal inline fun Dialog.addCloseButton(
    text: String,
    table: Table = this.cont,
    crossinline task: () -> Unit = {},
): Cell<TextButton> {
    return table.button(text) {
        Welcome.recordClick()
        task()
        hide()
    }.size(200f, 50f)
}

internal inline fun Dialog.createCloseButton(
    text: String,
    crossinline task: () -> Unit = {}
): TextButton {
    return Elem.newButton(text) {
        Welcome.recordClick()
        task()
        hide()
    }
}
