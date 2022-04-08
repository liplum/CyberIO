package net.liplum.npc

import arc.Events
import arc.scene.ui.Label
import arc.scene.ui.layout.Table
import arc.util.Log
import net.liplum.inputs.UnitTapEvent

object NPC {
    var curText =
        "To be, or not to be, that is the question:Whether 'tis nobler in the mind to sufferThe slings and arrows of outrageous fortune,Or to take arms against a sea of troublesAnd by opposing end them. To die—to sleep,No more; and by a sleep to say we endThe heart-ache and the thousand natural shocksThat flesh is heir to: 'tis a consummation."
    val textTable = Table().apply {
        add(Label { curText }.apply {
            setAlignment(10)
            setWrap(true)
        }).growX().row()
    }
    val dialog = NpcDialog("Npc Dialog").apply {
        cont.add(textTable).growX().row()
        addCloseButton()
        clicked {
            Log.info("Clicked.")
        }
    }

    @JvmStatic
    fun showDialog() {
        dialog.show()
    }

    @JvmStatic
    fun registerEvent() {
        Events.on(UnitTapEvent::class.java) {
            if (it.unit == it.player.unit()) {
                showDialog()
            }
        }
    }
}
