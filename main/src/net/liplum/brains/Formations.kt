package net.liplum.brains

import net.liplum.ClientOnly
import net.liplum.api.brain.*
import net.liplum.brains.Ear.EarBuild
import net.liplum.brains.Eye.EyeBuild

object FaceFE : SelfFormation(
    // Right
    null, EarBuild::class.java,
    // Top
    null, null,
    // Left
    null, EarBuild::class.java,
    // Bottom
    EyeBuild::class.java, EyeBuild::class.java,
), IFormationEffect {
    override val name = "Face"
    override val upgrades: Map<UpgradeType, Upgrade> = listOf(
        Upgrade(UT.Damage, true, 1f)
    ).toUpgradeMap()
    @ClientOnly
    override fun draw(brain: IBrain) {
        val sides = brain.sides
        val bottom = sides.bottom
        val components = bottom.components
        val eyeLeft = components[0] as? EyeBuild
        val eyeRight = components[1] as? EyeBuild
        if (eyeLeft != null && eyeRight != null) {
            syncEyeBlink(eyeLeft, eyeRight)
        }
    }
}

object FunnyFaceFE : SelfFormation(
    // Right
    null, EyeBuild::class.java,
    // Top
    null, null,
    // Left
    null, EyeBuild::class.java,
    // Bottom
    null, null,
), IFormationEffect {
    override val name = "FunnyFace"
    override val upgrades: Map<UpgradeType, Upgrade> = listOf(
        Upgrade(UT.PowerUse, true, -0.01f)
    ).toUpgradeMap()
    @ClientOnly
    override fun draw(brain: IBrain) {
        val sides = brain.sides
        val eyeLeft = sides.left[1] as? EyeBuild
        val eyeRight = sides.right[1] as? EyeBuild
        if (eyeLeft != null && eyeRight != null) {
            syncEyeBlink(eyeLeft, eyeRight)
        }
    }
}

object ForceFieldFE : IFormationPattern, IFormationEffect {
    override fun match(brain: IBrain): IFormationEffect? {
        val components = brain.components
        val eyeCount = components.count {
            it is EyeBuild
        }
        val earCount = components.count {
            it is EarBuild
        }
        return if (eyeCount >= 1 && earCount >= 2)
            this
        else
            null
    }

    override val enableShield = true
    override val upgrades: Map<UpgradeType, Upgrade> = listOf(
        Upgrade(UT.PowerUse, true, 0.5f)
    ).toUpgradeMap()
    override val name = "ForceField"
    override fun toString() = name
}

fun syncEyeBlink(eyeA: EyeBuild, eyeB: EyeBuild) {
    val res = findIn(eyeA, eyeB) {
        it.isShooting || it.charging
    }
    if (res == null) {
        val index = eyeA.blinkAnime.index
        val curTime = eyeA.blinkAnime.curTime
        eyeB.blinkAnime.index = index
        eyeB.blinkAnime.curTime = curTime
    } else {
        val (shootingEye, b) = res
        val index = shootingEye.blinkAnime.index
        val curTime = shootingEye.blinkAnime.curTime
        b.blinkAnime.index = index
        b.blinkAnime.curTime = curTime
        b.blinkFactor = 0f
    }
}
/**
 * Find the first eye meet the requirement, and return (match,another).
 *
 * If none meets the requirement, return null
 */
inline fun findIn(eyeA: EyeBuild, eyeB: EyeBuild, condition: (EyeBuild) -> Boolean): (Pair<EyeBuild, EyeBuild>)? {
    val isA = condition(eyeA)
    if (isA)
        return Pair(eyeA, eyeB)
    val isB = condition(eyeB)
    if (isB)
        return Pair(eyeB, eyeA)
    return null
}