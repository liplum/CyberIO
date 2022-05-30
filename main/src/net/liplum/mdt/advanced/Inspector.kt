package net.liplum.mdt.advanced

import arc.util.Time
import mindustry.Vars
import mindustry.game.EventType
import mindustry.gen.Building
import net.liplum.annotations.Only
import net.liplum.annotations.Subscribe
import net.liplum.inputs.Screen
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.IFocusable

object Inspector {
    @ClientOnly
    var preSelected: Building? = null
        private set
    @ClientOnly
    var curSelected: Building? = null
        private set
    @ClientOnly
    var selectingTime = 0f
        private set
    @ClientOnly
    @Subscribe(EventType.Trigger.preDraw, Only.client)
    fun updateSelectedTile() {
        if (Vars.state.isMenu) return
        // Reset the selecting time when not select any building
        if (curSelected != null) selectingTime += Time.delta
        else selectingTime = 0f
        // Update the selected state
        val input = Vars.control.input
        if (input.selectedBlock()) {
            setCurSelected(null)
        } else {
            val build = Screen.buildOnMouse()
            setCurSelected(build)
        }
    }
    @ClientOnly
    fun Building.isSelectedByMouse(): Boolean =
        this == curSelected
    @ClientOnly
    private fun setCurSelected(build: Building?) {
        if (curSelected != build) {
            (preSelected as? IFocusable)?.onLostFocus()
            preSelected = curSelected
            curSelected = build
            selectingTime = 0f
            (curSelected as? IFocusable)?.onFocused()
        }
    }
}