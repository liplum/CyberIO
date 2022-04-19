package net.liplum.brains

import net.liplum.ClientOnly
import net.liplum.api.brain.IUpgradeComponent
import net.liplum.api.brain.Side2

interface IFormationPattern {
    fun match(sides: Array<Side2>): IFormationEffect?
}

interface IFormationEffect {
    fun update(sides: Array<Side2>)
    @ClientOnly
    fun draw(sides: Array<Side2>)
}

object EmptyFormationEffect : IFormationEffect {
    override fun update(sides: Array<Side2>) {
    }

    override fun draw(sides: Array<Side2>) {
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

    override fun match(sides: Array<Side2>): IFormationEffect? {
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