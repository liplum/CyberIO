package net.liplum.api.brain

import net.liplum.common.toLinkedString
import plumy.core.ClientOnly

interface IFormationPattern {
    fun match(brain: IBrain): IFormationEffect?
}

interface IFormationEffect {
    val name: String
    val enableShield: Boolean
        get() = false
    val upgrades: Map<UpgradeType, Upgrade>
    fun update(brain: IBrain) {}
    @ClientOnly
    fun draw(brain: IBrain) {
    }
}

class FormationEffects(
    val all: Set<IFormationEffect>,
) : Iterable<IFormationEffect> {
    val enableShield = all.any {
        it.enableShield
    }
    val deltaUpgrades = composeUpgrades(isDelta = true)
    val rateUpgrades = composeUpgrades(isDelta = false)
    fun composeUpgrades(isDelta: Boolean): Map<UpgradeType, UpgradeEntry> {
        val res = HashMap<UpgradeType, UpgradeEntry>()
        for (effect in all) {
            for (up in effect.upgrades.filter {
                if (isDelta) it.value.isDelta else !it.value.isDelta
            }) {
                val upgrade = res.getOrPut(up.key) { UpgradeEntry() }
                upgrade.value += up.value.value
            }
        }
        return res
    }

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
    override val upgrades: Map<UpgradeType, Upgrade> = emptyMap()
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

    override fun toString() = "[0]${Part0?.simpleName}[1]${Part1?.simpleName}"
}

open class FixedFormation(
    var effect: IFormationEffect,
    vararg val sidePatterns: Side2Pattern,
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
/**
 * ```
 * ┌───┬───┬───┬───┐
 * │   │1.1│1.0│   │
 * ├───┼───┼───┼───┤
 * │2.0│ x │ x │0.1│
 * ├───┼───┼───┼───┤
 * │2.1│ x │ x │0.0│
 * ├───┼───┼───┼───┤
 * │   │3.0│3.1│   │
 * └───┴───┴───┴───┘
 * ```
 */
abstract class SelfFormation(
    vararg components: Class<out IUpgradeComponent>?,
) : FixedFormation(
    EmptyFormationEffect, *analyzeSides(components)
), IFormationEffect {
    init {
        effect = this
    }

    override val upgrades: Map<UpgradeType, Upgrade> = emptyMap()
    override fun toString() = name
}
/**
 * ```
 * ┌───┬───┬───┬───┐
 * │   │1.1│1.0│   │
 * ├───┼───┼───┼───┤
 * │2.0│ x │ x │0.1│
 * ├───┼───┼───┼───┤
 * │2.1│ x │ x │0.0│
 * ├───┼───┼───┼───┤
 * │   │3.0│3.1│   │
 * └───┴───┴───┴───┘
 * ```
 */
fun analyzeSides(com: Array<out Class<out IUpgradeComponent>?>)
        : Array<Side2Pattern> =
    Array(4) {
        Side2Pattern(com[2 * it], com[2 * it + 1])
    }

fun List<Upgrade>.toUpgradeMap(): Map<UpgradeType, Upgrade> =
    this.associateBy { it.type }

interface IRotatedFormationPattern : IFormationPattern {
    override fun match(brain: IBrain): IFormationEffect? {
        val cur = brain.sides.copyInto(tmp)
        var i = 0
        do {
            val matched = mappedMatch(brain, cur)
            if (matched != null) return matched
            if (i < 3)// only need rotate third actually
                cur.clockwiseRotate()
            i++
        } while (i < 4)
        return null
    }
    /**
     * Overwrite this function, and use [sides] parameter instead of [IBrain.sides]
     */
    fun mappedMatch(brain: IBrain, sides: Sides): IFormationEffect?

    companion object {
        @Suppress("UNCHECKED_CAST")
        val tmp = Sides(arrayOfNulls<Side2>(4) as Array<Side2>)
    }
}

open class FixedRotatedFormation(
    var effect: IFormationEffect,
    vararg val sidePatterns: Side2Pattern,
) : IRotatedFormationPattern {
    init {
        assert(sidePatterns.size != 4) {
            "${sidePatterns.size} isn't 4."
        }
    }

    override fun mappedMatch(brain: IBrain, sides: Sides): IFormationEffect? {
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

abstract class SelfRotatedFormation(
    vararg components: Class<out IUpgradeComponent>?,
) : FixedRotatedFormation(
    EmptyFormationEffect, *analyzeSides(components)
), IFormationEffect {
    init {
        effect = this
    }

    override val upgrades: Map<UpgradeType, Upgrade> = emptyMap()
    override fun toString() = name
}
