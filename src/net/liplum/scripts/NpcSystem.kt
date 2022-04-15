package net.liplum.scripts

import arc.Events
import mindustry.Vars
import net.liplum.inputs.UnitTapEvent
import net.liplum.utils.MdtUnit

object NpcSystem {
    var curText =
        "To be, or not to be, that is the question."
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
                //showDialog()
            }
        }
    }
}
