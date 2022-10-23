package net.liplum.event

import arc.Events
import arc.input.InputProcessor
import arc.input.KeyCode
import mindustry.Vars
import mindustry.entities.Units
import mindustry.gen.Player
import net.liplum.utils.Screen
import plumy.core.ClientOnly
import plumy.core.MUnit

@ClientOnly
data class UnitTapEvent(val player: Player, val unit: MUnit)
@ClientOnly
object UnitTap : InputProcessor {
    @ClientOnly
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: KeyCode): Boolean {
        if (!Vars.state.isGame) return false
        val worldXY = Screen.toWorld(screenX, screenY)
        val player = Vars.player
        if (player != null) {
            val closest: MUnit? = Units.closest(Vars.player.team(), worldXY.x, worldXY.y) {
                !it.dead && it.dst(worldXY.x, worldXY.y) < 10f
            }
            if (closest != null) {
                Events.fire(UnitTapEvent(Vars.player, closest))
            }
        }
        return false
    }
}