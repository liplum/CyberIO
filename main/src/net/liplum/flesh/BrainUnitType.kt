package net.liplum.flesh

import arc.func.Prov
import mindustry.ai.types.GroundAI
import mindustry.entities.Units
import mindustry.gen.Teamc
import mindustry.type.UnitType

class BrainUnitType(name: String) : UnitType(name) {
    init {
        aiController = Prov {
            object : GroundAI() {
                override fun updateMovement() {
                    val nearest: Teamc? = Units.closestTarget(
                        unit.team, unit.x, unit.y, 200f
                    )
                    if (nearest != null) {
                        target = nearest
                        this.moveTo(nearest, 16f)
                    } else {
                        super.updateMovement()
                    }
                }
            }
        }
    }
}