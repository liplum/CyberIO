package net.liplum.ui.template

import arc.Core
import arc.scene.style.Drawable
import arc.scene.ui.Image
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Scl

fun NewIconTextButton(
    text: String,
    icon: Drawable,
    iconSize: Float,
    clicked: () -> Unit,
) = NewIconTextButton(
    text, icon, iconSize,
    Core.scene.getStyle(TextButton.TextButtonStyle::class.java),
    clicked
)

fun NewIconTextButton(
    text: String,
    icon: Drawable,
    iconSize: Float,
    style: TextButton.TextButtonStyle,
    clicked: () -> Unit,
) = TextButton(text, style).apply {
    add(Image(icon)).size(iconSize)
    cells.reverse()
    clicked(clicked)
}

fun NewIconTextButton(
    text: String,
    icon: Drawable,
    style: TextButton.TextButtonStyle =
        Core.scene.getStyle(TextButton.TextButtonStyle::class.java),
    clicked: () -> Unit,
) = TextButton(text, style).apply {
    add(Image(icon)).size(icon.imageSize() / Scl.scl(1f))
    cells.reverse()
    clicked(clicked)
}
