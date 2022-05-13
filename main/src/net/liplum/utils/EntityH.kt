package net.liplum.utils

import mindustry.gen.*
import mindustry.world.blocks.ControlBlock

var Healthc.lostHp: Float
    get() = maxHealth() - health()
    set(value) {
        health(maxHealth() - value)
    }
var Healthc.lostHpPct: Float
    get() = 1f - healthPct
    set(value) {
        health((1 - value.coerceIn(0f, 1f)) * maxHealth())
    }
var Healthc.healthPct: Float
    get() = (healthf()).coerceIn(0f, 1f)
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
/*
TODO: Because of the real RTS,
  player cannot shoot plentiful bullets once when they control a group.
fun MdtUnit.findLeaderInFormation(): MdtUnit {
    val controller = this.controller()
    if (controller is FormationAI) {
        return controller.leader
    }
    return this
}

fun Building.otherConsumersAreValid(consume: Consume): Boolean {
    for (cons in this.block.consumers) {
        // TODO: Migration
// if (cons != consume && !cons.optional && cons.efficiency(this) <= 0f) {
        if (cons != consume && !cons.optional && !cons.valid(this)) {
            return false
        }
    }
    return true
}
*/
