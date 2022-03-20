package net.liplum.holo

import mindustry.entities.abilities.Ability
import mindustry.gen.Unit

open class HoloAbility() : Ability() {
    var lose = 0.5f

    constructor(lose: Float) : this() {
        this.lose = lose
    }

    override fun update(unit: Unit) {
        unit.damageContinuousPierce(lose)
    }
}