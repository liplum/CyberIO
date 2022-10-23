package net.liplum.statusFx

import arc.math.Mathf
import arc.util.Tmp
import mindustry.content.Fx
import mindustry.gen.Groups
import mindustry.gen.Unit
import mindustry.graphics.Drawf
import mindustry.type.StatusEffect
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.utils.lostHp

enum class DamageMode {
    MaxHP, CurHP, LostHP
}

open class Infected(name: String) : StatusEffect(name) {
    @JvmField var range = 50f
    @JvmField var initTime = 10 * 60f
    @JvmField var damageProportion = 1f / 10f / 60f
    @JvmField var damageMode = DamageMode.CurHP

    init {
        color = R.C.VirusBK
        effect = Fx.sporeSlowed
    }

    override fun update(unit: Unit, time: Float) {
        val damage = when (damageMode) {
            DamageMode.MaxHP -> unit.maxHealth() * damageProportion
            DamageMode.CurHP -> unit.health() * damageProportion
            DamageMode.LostHP -> unit.lostHp * damageProportion
        }
        unit.damageContinuousPierce(damage)
        if (effect !== Fx.none && Mathf.chanceDelta(effectChance.toDouble())) {
            Tmp.v1.rnd(Mathf.range(unit.type.hitSize / 2f))
            effect.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, 0f, color, if (parentizeEffect) unit else null)
        }
        if (unit.dead()) {
            spread(unit)
        }
    }

    override fun draw(unit: Unit) {
        super.draw(unit)
        DebugOnly {
            Drawf.circles(unit.x, unit.y, range, R.C.VirusBK)
        }
    }

    open fun spread(body: Unit) {
        Groups.unit.intersect(
            body.x - range / 2f, body.y - range / 2f,
            range, range
        ) {
            if (body != it &&
                !it.dead() &&
                body.dst(it) <= range &&
                it.team() == body.team
            ) {
                it.apply(this, initTime)
            }
        }
    }
}