package net.liplum.scripts

import mindustry.gen.UnitEntity
import net.liplum.registries.EntityRegistry

/**
 * Npc unit is invulnerable.
 */
open class NpcUnit : UnitEntity() {
    override fun classId(): Int {
        return EntityRegistry.getID(javaClass)
    }
    /**
     * Although a Npc is actually AI, this is done to prevent it from being controlled by player.
     */
    override fun isAI() = false
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