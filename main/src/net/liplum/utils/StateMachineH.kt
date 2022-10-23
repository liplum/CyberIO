package net.liplum.utils

import mindustry.gen.Building
import net.liplum.CanRefresh
import plumy.core.WhenNotPaused
import plumy.animation.state.StateMachine
import plumy.core.arc.Tick

fun <T : Building> StateMachine<T>.update(delta: Tick) {
    WhenNotPaused {
        spend(delta)
    }
    if (CanRefresh()) {
        updateState()
    }
}