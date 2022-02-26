package net.liplum.utils

import mindustry.gen.Healthc

var Healthc.lostHp: Float
    get() = this.maxHealth() - this.health()
    set(value) {
        this.health(this.maxHealth() - value)
    }
var Healthc.lostHpPct: Float
    get() = (this.maxHealth() - this.health()) / this.maxHealth()
    set(value) {
        this.health((1 - value.coerceIn(0f, 1f)) * this.maxHealth())
    }