package net.liplum.brains

import net.liplum.ClientOnly
import net.liplum.api.brain.IBrain
import net.liplum.api.brain.IUpgradeComponent
import net.liplum.lib.toLinkedString

interface IFormationPattern {
    fun match(brain: IBrain): IFormationEffect?
}

interface IFormationEffect {
    val name: String
    val enableShield: Boolean
        get() = false

    fun update(brain: IBrain) {}
    @ClientOnly
    fun draw(brain: IBrain) {
    }
}

class FormationEffects(
    val all: Set<IFormationEffect>
) : Iterable<IFormationEffect> {
    val enableShield = all.find {
        it.enableShield
    } != null
    val isEmpty = all.isEmpty()
    val isNotEmpty = all.isNotEmpty()
    operator fun contains(effect: IFormationEffect) = effect in all
    override fun toString() = if (isEmpty) "None" else all.toLinkedString()
    override fun iterator() = all.iterator()
    fun update(brain: IBrain) {
        for (effect in all)
            effect.update(brain)
    }
    @ClientOnly
    fun draw(brain: IBrain) {
        for (effect in all)
            effect.draw(brain)
    }

    companion object {
        @JvmField
        val Empty = FormationEffects(emptySet())
    }
}

object EmptyFormationEffect : IFormationEffect {
    override val name = "Empty"
    override fun update(brain: IBrain) {
    }

    override fun draw(brain: IBrain) {
    }
}

class Side2Pattern(
    val Part0: Class<out IUpgradeComponent>? = null,
    val Part1: Class<out IUpgradeComponent>? = null,
) {
    operator fun get(part: Int): Class<out IUpgradeComponent>? {
        return if (part == 0)
            Part0
        else
            Part1
    }
}

open class Formation(
    var effect: IFormationEffect,
    vararg val sidePatterns: Side2Pattern
) : IFormationPattern {
    init {
        assert(sidePatterns.size != 4) {
            "${sidePatterns.size} isn't 4."
        }
    }

    override fun match(brain: IBrain): IFormationEffect? {
        val sides = brain.sides
        for (i in 0..3) {
            val s = sides[i]
            val p = sidePatterns[i]
            for (j in 0..1) {
                val com = s[j]
                val req = p[j]
                if (req == null) {
                    if (com != null)
                        return null
                } else {
                    if (com == null)
                        return null
                    else if (!req.isInstance(com))
                        return null
                }
            }
        }
        return effect
    }
}

abstract class SelfFormation(vararg components: Class<out IUpgradeComponent>?) : Formation(
    EmptyFormationEffect, *analyzeSides(components)
), IFormationEffect {
    init {
        effect = this
    }
    override fun toString() = name
}

fun analyzeSides(com: Array<out Class<out IUpgradeComponent>?>)
        : Array<Side2Pattern> =
    Array(4) {
        Side2Pattern(com[2 * it], com[2 * it + 1])
    }
