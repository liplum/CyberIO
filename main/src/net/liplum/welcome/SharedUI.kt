package net.liplum.welcome

import arc.graphics.Texture.TextureFilter.nearest
import arc.scene.ui.Dialog
import arc.scene.ui.Image
import arc.scene.ui.Label
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
import arc.util.Scaling
import mindustry.gen.Tex
import net.liplum.Meta
import net.liplum.Var
import net.liplum.i18nName
import plumy.core.DesktopOnly
import plumy.core.assets.TR

internal fun Dialog.addPoster(
    icon: TR,
    table: Table = this.cont,
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

internal fun Dialog.addPoliteWelcome(entity: WelcomeEntity) {
    addCenterText(
        entity.bundle.format(
            "welcome",
            "${Meta.DetailedVersion} ${Var.ContentSpecific.i18nName}"
        )
    ).color(Var.Hologram)
}

internal fun Dialog.addCenterText(
    text: String,
    table: Table = this.cont,
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

internal fun Dialog.addBoxedText(
    text: String,
    table: Table = this.cont,
): Cell<Table> {
    val cell = table.add(Table(Tex.button).apply {
        add(Label(text).apply {
            DesktopOnly {
                // On the high resolution screen, the text looks too small.
                setFontScale(1.1f)
            }
        }).pad(10f)
    })
    cell.pad(5f).row()
    return cell
}

internal inline fun Dialog.addCloseButton(
    text: String,
    table: Table = this.cont,
    width: Float = 200f,
    crossinline task: () -> Unit = {},
): Cell<TextButton> {
    return table.button(text) {
        Welcome.recordClick()
        task()
        hide()
    }.size(width, 50f)
}
