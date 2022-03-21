package net.liplum.utils

import mindustry.gen.Healthc
import mindustry.gen.UnitEntity

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