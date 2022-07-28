package net.liplum.event

import arc.Events
import mindustry.core.GameState
import mindustry.game.EventType
import mindustry.mod.Mod
import net.liplum.annotations.SubscribeEvent

/**
 * It will be raised at the head of [Mod.init]
 */
class CioInitEvent
/**
 * It will be raised at the head of [Mod.loadContent]
 * ,before all Contents in Cyber are loaded.
 * ### Before:
 * 1. Contents
 * 2. Techtree
 */
class CioLoadContentEvent
/**
 * It will be raised when a certain game, like sandbox, survival or pvp, ends on Client or Server.
 */
class BattleExitEvent
@SubscribeEvent(EventType.StateChangeEvent::class)
fun raiseBattleExitEvent(e: EventType.StateChangeEvent) {
    if (e.to == GameState.State.menu) {
        Events.fire(BattleExitEvent())
    }
}