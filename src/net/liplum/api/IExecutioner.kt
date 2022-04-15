package net.liplum.api

import mindustry.gen.Healthc
import net.liplum.api.holo.IHoloEntity

interface IExecutioner {
    val executeProportion: Float
    val Healthc.executeLine: Float
        get() = this.maxHealth() * executeProportion
    val Healthc.canBeExecuted: Boolean
        get() = this.health() <= executeLine

    fun execute(entity: Healthc, withEffect: Boolean = false) {
        if (withEffect) {
            entity.damagePierce(entity.maxHealth(), true)
        } else {
            entity.damagePierce(entity.maxHealth())
        }
        if (entity is IHoloEntity) {
            entity.killThoroughly()
        }
    }
    /**
     * Try to execute an entity.
     * @return whether it is executed.
     */
    fun tryExecute(entity: Healthc, withEffect: Boolean = false): Boolean {
        if (entity.canBeExecuted) {
            execute(entity, withEffect)
            return true
        }
        return false
    }
}