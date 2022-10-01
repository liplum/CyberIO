package net.liplum.brain

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
import net.liplum.api.brain.*
import net.liplum.brain.Ear.EarBuild
import net.liplum.brain.Eye.EyeBuild
import plumy.core.ClientOnly

private val EYE = EyeBuild::class.java
private val EAR = EarBuild::class.java

object FaceFE : SelfRotatedFormation(
    // Right
    EAR, null,
    // Top
    null, null,
    // Left
    null, EAR,
    // Bottom
    EYE, EYE,
), IFormationEffect {
    override val name = "Face"
    override val upgrades: Map<UpgradeType, Upgrade> = listOf(
        Upgrade(UT.Damage, true, 1f)
    ).toUpgradeMap()
    @ClientOnly
    override fun draw(brain: IBrain) {
        val components = brain.components
        val eye = components.filterIsInstance<EyeBuild>()
        if (eye.size == 2) {
            syncEyeBlink(eye[0], eye[1])
        }
    }
}

object FunnyFaceFE : SelfFormation(
    EYE, null,
    // Top
    null, null,
    // Left
    null, EYE,
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
        return if (eyeCount >= 2 && earCount >= 2)
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

object LootAtHeartFE : SelfRotatedFormation(
) {
    override val name = "LootAtHeart"
    override val upgrades: Map<UpgradeType, Upgrade> = emptyMap()
}

fun syncEyeBlink(eyeA: EyeBuild, eyeB: EyeBuild) {
    val res = findIn(eyeA, eyeB) {
        it.isShooting || it.charging()
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