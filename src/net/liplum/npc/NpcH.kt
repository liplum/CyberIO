package net.liplum.npc

import arc.Events
import mindustry.Vars
import net.liplum.Script
import net.liplum.inputs.UnitTapEvent
import net.liplum.utils.MdtUnit

object NpcSystem {
    var curText =
        "To be, or not to be, that is the question:Whether 'tis nobler in the mind to sufferThe slings and arrows of outrageous fortune,Or to take arms against a sea of troublesAnd by opposing end them. To die—to sleep,No more; and by a sleep to say we endThe heart-ache and the thousand natural shocksThat flesh is heir to: 'tis a consummation."
    var showNpcDialog = false
    var curNpc : MdtUnit? = null
    val npcDialog = NpcDialogFrag().apply {
        showDialog = { showNpcDialog }
        text = { curText }
        onGoNext.add {
            Script.goNext()
        }
    }

    @JvmStatic
    fun showDialog() {
        showNpcDialog = true
    }

    @JvmStatic
    fun closeDialog(){
        showNpcDialog = false
    }

    @JvmStatic
    fun register() {
        val hudGroup = Vars.ui.hudGroup
        npcDialog.buildDialog()
        npcDialog.build(hudGroup)
        Events.on(UnitTapEvent::class.java) {
            if (it.unit == it.player.unit()) {
                showDialog()
            }
        }
    }
}
