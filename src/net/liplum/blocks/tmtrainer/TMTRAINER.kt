package net.liplum.blocks.tmtrainer

import mindustry.content.Fx
import mindustry.world.blocks.defense.turrets.ItemTurret

class TMTRAINER(name: String) : ItemTurret(name) {
    init {
        spread = 4f
        shots = 2
        alternate = true
        reloadTime = 5f
        restitution = 0.03f
        range = 110f
        shootCone = 15f
        ammoUseEffect = Fx.sporeSlowed
        health = 250
        inaccuracy = 1f
        rotateSpeed = 10f

        limitRange()
    }
}