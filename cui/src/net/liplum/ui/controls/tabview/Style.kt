package net.liplum.ui.controls.tabview

import arc.Core
import arc.scene.style.Drawable
import arc.scene.ui.Button
import net.liplum.ui.TRD

class TabViewStyle {
    var contentViewStyle: Drawable = emptyContentViewStyle
    var tabOptionStyle: Button.ButtonStyle =
        Core.scene.getStyle(Button.ButtonStyle::class.java)

    companion object {
        val emptyContentViewStyle = TRD()
    }
}