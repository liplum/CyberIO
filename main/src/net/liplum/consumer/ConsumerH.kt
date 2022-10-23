package net.liplum.consumer

import arc.math.Mathf
import mindustry.gen.Building

var Building.powerStore: Float
    get() = if (power == null || block.consPower == null) 0f else power.status * block.consPower.capacity
    set(value) {
        if (power == null || block.consPower == null) return
        power.status = Mathf.clamp(value / block.consPower.capacity)
    }
