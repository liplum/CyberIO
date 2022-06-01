package net.liplum.mdt.advanced

import arc.util.Time
import mindustry.Vars
import mindustry.game.EventType
import mindustry.gen.Building
import mindustry.world.Block
import net.liplum.annotations.Only
import net.liplum.annotations.Subscribe
import net.liplum.inputs.Screen
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.IFocusable

@ClientOnly
object Inspector {
    var preSelected: Building? = null
        private set
    var curSelected: Building? = null
        private set
    var selectingTime = 0f
        private set
    var prePlacing: Block? = null
    var curPlacing: Block? = null
        private set
    var placingTime = 0f
        private set
    @Subscribe(EventType.Trigger.preDraw, Only.client)
    fun updateInput() {
        updateSelectedTile()
        updatePlacingBlock()
    }

    fun updatePlacingBlock() {
        if (Vars.state.isMenu) return
        if (curPlacing != null) placingTime += Time.delta
        else placingTime = 0f
        setCurPlacing(Vars.control.input.block)
    }

    private fun setCurPlacing(block: Block?) {
        if (curPlacing != block) {
            prePlacing = curPlacing
            curPlacing = block
            placingTime = 0f
        }
    }

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

    fun Building.isSelected(): Boolean =
        this == curSelected

    fun Block.isPlacing(): Boolean =
        this == curPlacing

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