package net.liplum.holo

import arc.scene.ui.layout.Table
import arc.util.Time
import mindustry.entities.abilities.Ability
import mindustry.gen.Unit
import mindustry.gen.UnitEntity
import mindustry.ui.Bar
import net.liplum.DebugOnly
import net.liplum.Else
import net.liplum.R
import net.liplum.abilites.localized
import net.liplum.seconds
import net.liplum.utils.bundle
import net.liplum.utils.hasShields

open class HoloAbility() : Ability() {
    var lose = 0.3f
    var lifespan = 120 * 60f
    var time = 0f

    companion object {
        @JvmStatic
        fun byLose(lose: Float): HoloAbility {
            return HoloAbility().apply {
                this.lose = lose
            }
        }
        @JvmStatic
        fun byLifespan(lifespan: Float): HoloAbility {
            return HoloAbility().apply {
                this.lifespan = lifespan
            }
        }

        fun HoloUnitType.AutoLifespan(lose: Float): Float {
            return this.health / lose
        }
    }

    constructor(lose: Float, lifespan: Float) : this() {
        this.lose = lose
        this.lifespan = lifespan
    }

    override fun update(unit: Unit) {
        time += Time.delta
        var damage = lose
        val overage = time - lifespan
        if (overage > 0) {
            damage += overage * lose * 0.5f
        }
        damageUnit(unit, damage)
    }

    val restLifePercent: Float
        get() = (1f - (time / lifespan)).coerceIn(0f, 1f)
    val restLife: Float
        get() = (lifespan - time).coerceIn(0f, lifespan)

    override fun displayBars(unit: Unit, bars: Table) {
        DebugOnly {
            bars.add(Bar(
                { R.Bar.RestLifeFigure.bundle(restLife.seconds) },
                { R.C.Holo },
                { restLifePercent }
            ))
        }.Else {
            bars.add(Bar(
                { R.Bar.RestLife.bundle },
                { R.C.Holo },
                { restLifePercent }
            ))
        }
        bars.row()
    }

    open fun damageUnit(unit: Unit, amount: Float) {
        if (unit is UnitEntity && unit.hasShields) {
            unit.shieldAlpha = 1.0f
            unit.health -= amount
            unit.hitTime = 1.0f
            if (unit.health <= 0.0f && !unit.dead) {
                unit.kill()
            }
        } else {
            unit.damageContinuousPierce(amount)
        }
    }

    override fun localized() =
        this.javaClass.localized()
}