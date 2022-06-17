package net.liplum.scripts

import arc.scene.Group
import arc.scene.ui.Label
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
import mindustry.gen.Tex
import net.liplum.common.delegates.Delegate

enum class DialogMode {
    Text, Option
}

open class NpcDialogFrag {
    var showDialog = { false }
    var text = { "" }
    var onGoNext = Delegate()
    var onSwitchMode = Delegate()
    var dialogTable = Table()
    var textMode = Table()
    var optionMode = Table()
    var mode = DialogMode.Text
    fun build(parent: Group) {
        parent.fill { t ->
            t.name = "npc dialog"
            t.center()
            t.visible(showDialog)
            t.add(dialogTable)
            t.setSize(500f, 100f)
        }
    }

    lateinit var contentCell: Cell<Table>
    fun buildDialog() {
        textMode.apply {
            background(Tex.pane)
            setFillParent(true)
            add(Label(text).apply {
                setWrap(true)
                setAlignment(0)
            }).size(500f, 100f).growX().row()
            clicked {
                onGoNext()
            }
        }
        dialogTable.apply {
            contentCell = dialogTable.add(textMode)
        }
        onSwitchMode.add {
            when (mode) {
                DialogMode.Text -> contentCell.setElement(textMode)
                DialogMode.Option -> contentCell.setElement(textMode)
            }
            dialogTable.layout()
        }
    }
}