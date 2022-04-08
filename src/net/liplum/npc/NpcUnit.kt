package net.liplum.npc

import mindustry.gen.UnitEntity
import net.liplum.registries.EntityRegistry

open class NpcUnit : UnitEntity() {
    override fun classId(): Int {
        return EntityRegistry.getID(javaClass)
    }
    override fun damageMultiplier() = 0f
    override fun damageMultiplier(damageMultiplier: Float) {}
    override fun damaged() = false
    override fun damagePierce(amount: Float, withEffect: Boolean) {}
    override fun damagePierce(amount: Float) {}
    override fun damage(amount: Float) {}
    override fun damage(amount: Float, withEffect: Boolean) {}
    override fun damageContinuous(amount: Float) {}
    override fun damageContinuousPierce(amount: Float) {}

}