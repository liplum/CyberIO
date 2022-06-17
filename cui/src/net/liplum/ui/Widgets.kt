package net.liplum.ui

import arc.graphics.Color
import arc.scene.ui.layout.Table

fun Table.addSeparatorLine(
    color: Color = Color.white,
) {
    this.image().growX().pad(5f).padLeft(0f).padRight(0f).height(3f).color(color).row()
}