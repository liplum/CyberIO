package net.liplum.ui

import arc.scene.ui.layout.Table
import mindustry.ctype.UnlockableContent
import mindustry.gen.Tex
import net.liplum.utils.ForEachUnlockableContent

fun Table.lockOrUnlock(
    text: String,
    onClick: (UnlockableContent) -> Unit,
    filter: ((UnlockableContent) -> Boolean)? = null,
) {
    this.button(text) {
        NewBaseDialog.apply {
            cont.table(Tex.button) { t ->
                t.button("$text All") {
                    ForEachUnlockableContent {
                        onClick(it)
                    }
                }.growX().row()
                t.button("Select One To $text") {
                    DatabaseSelectorDialog.apply {
                        filter?.let {
                            this.filter = it
                        }
                        this.onClick = {
                            NewBaseDialog.apply {
                                cont.add("Confirm $text ${it.localizedName} ?").row()
                                cont.button(text) {
                                    onClick(it)
                                    DatabaseSelectorDialog.rebuild()
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