package net.liplum.brains

import net.liplum.ClientOnly
import net.liplum.api.brain.Side2
import net.liplum.api.brain.bottom

object FaceFE : Formation(
    EmptyFormationEffect,
    // Right
    Side2Pattern(
        null, Ear.EarBuild::class.java
    ),
    // Top
    Side2Pattern(),
    // Left
    Side2Pattern(
        null, Ear.EarBuild::class.java
    ),
    // Bottom
    Side2Pattern(
        Eye.EyeBuild::class.java, Eye.EyeBuild::class.java
    ),
), IFormationEffect {
    init {
        effect = FaceFE
    }
    override fun update(sides: Array<Side2>) {
    }
    @ClientOnly
    override fun draw(sides: Array<Side2>) {
        val bottom = sides.bottom
        val components = bottom.components
        val eyeLeft = components[0] as? Eye.EyeBuild
        val eyeRight = components[1] as? Eye.EyeBuild
        if (eyeLeft != null && eyeRight != null) {
            val index = eyeLeft.blinkAnime.index
            val curTime = eyeLeft.blinkAnime.curTime
            eyeRight.blinkAnime.index = index
            eyeRight.blinkAnime.curTime = curTime
        }
    }
}