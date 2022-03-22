package net.liplum.utils

import mindustry.ai.types.FormationAI
import mindustry.gen.*
import mindustry.world.blocks.ControlBlock

var Healthc.lostHp: Float
    get() = maxHealth() - health()
    set(value) {
        health(maxHealth() - value)
    }
var Healthc.lostHpPct: Float
    get() = (maxHealth() - health()) / maxHealth()
    set(value) {
        health((1 - value.coerceIn(0f, 1f)) * maxHealth())
    }
var Healthc.healthPct: Float
    get() = (health() / maxHealth()).coerceIn(0f, 1f)
    set(value) {
        health(value.coerceIn(0f, 1f) * maxHealth())
    }
val UnitEntity.hasShields: Boolean
    get() = shield > 1.0E-4f

fun Entityc?.findPlayer(): Player? {
    if (this == null) return null
    if (this is ControlBlock) {
        return if (this.isControlled)
            this.unit().player
        else
            null
    } else if (this is Unitc) {
        return this.player
    }
    return null
}
typealias MdtUnit = mindustry.gen.Unit

fun MdtUnit.findLeaderInFormation(): MdtUnit {
    val controller = this.controller()
    if (controller is FormationAI) {
        return controller.leader
    }
    return this
}