package net.liplum.statusFx

import arc.math.Mathf
import arc.util.Tmp
import mindustry.content.Fx
import mindustry.gen.Unit
import mindustry.type.StatusEffect
import net.liplum.api.IExecutioner

open class Vulnerable(name: String) : StatusEffect(name), IExecutioner {
    override var executeProportion = 0.3f

    init {
        damage = 0f
    }

    override fun update(unit: Unit, time: Float) {
        tryExecute(unit, true)
        if (effect !== Fx.none && Mathf.chanceDelta(effectChance.toDouble())) {
            Tmp.v1.rnd(Mathf.range(unit.type.hitSize / 2f))
            effect.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, 0f, color, if (parentizeEffect) unit else null)
        }
    }
}