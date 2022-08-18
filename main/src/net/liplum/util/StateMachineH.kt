package net.liplum.util

import mindustry.gen.Building
import net.liplum.CanRefresh
import net.liplum.mdt.WhenNotPaused
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