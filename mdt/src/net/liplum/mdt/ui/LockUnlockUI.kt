package net.liplum.mdt.ui

import arc.scene.ui.layout.Table
import mindustry.ctype.UnlockableContent
import mindustry.gen.Tex
import net.liplum.mdt.lock
import net.liplum.mdt.utils.ForEachUnlockableContent

fun Table.lockOrUnlock(text: String, action: UnlockableContent.() -> Unit) {
    this.button(text) {
        NewBaseDialog.apply {
            cont.table(Tex.button) { t ->
                t.button("$text All") {
                    ForEachUnlockableContent {
                        it.action()
                    }
                }.growX().row()
                t.button("Select One To $text") {
                    DatabaseSelectorDialog.apply {
                        onClick = {
                            NewBaseDialog.apply {
                                cont.add("Confirm $text ${it.localizedName} ?").row()
                                cont.button(text) {
                                    it.action()
                                    hide()
                                }.width(150f)
                                addCloseButton()
                            }.show()
                        }
                    }.show()
                }.growX().row()
            }.width(300f)
            addCloseButton()
        }.show()
    }.width(150f)
}