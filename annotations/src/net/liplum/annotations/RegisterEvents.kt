package net.liplum.annotations

import mindustry.game.EventType
import kotlin.reflect.KClass

/**
 * Subscribe the target event automatically.
 */
annotation class Subscribe(
    val triggerType: EventType.Trigger,
    val only: Int = 0,
)
/**
 * Subscribe the target event automatically.
 */
annotation class SubscribeEvent(
    val eventClz: KClass<*>,
    val only: Int = 0,
)
