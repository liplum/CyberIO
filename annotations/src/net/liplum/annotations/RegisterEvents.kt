package net.liplum.annotations

import mindustry.game.EventType

/**
 * Subscribe the target event automatically.
 */
annotation class Subscribe(
    val eventType: EventType.Trigger,
    val clientOnly: Boolean = false,
    val debugOnly: Boolean = false,
    val headlessOnly: Boolean = false,
    val steamOnly: Boolean = false,
    val unsteamOnly: Boolean = false,
    val desktopOnly: Boolean = false,
    val mobileOnly: Boolean = false,
)