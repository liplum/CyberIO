package net.liplum.blocks.icmachine

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import mindustry.graphics.Drawf
import net.liplum.animations.anims.IAnimated
import net.liplum.animations.anis.AniConfig
import net.liplum.animations.anis.AniState
import net.liplum.blocks.AniedCrafter
import net.liplum.utils.TR
import net.liplum.utils.autoAnim
import net.liplum.utils.subA

private typealias AniStateM = AniState<AniedCrafter, AniedCrafter.AniedCrafterBuild>

open class ICMachine(name: String) : AniedCrafter(name) {
    lateinit var idleState: AniStateM
    lateinit var WorkingState: AniStateM
    lateinit var WorkingAnimation: IAnimated
    lateinit var idleTR: TR
    var WorkingAnimFrameNumber = 7
    var WorkingAnimDuration = 60f

    init {
        hasPower = true
    }

    override fun genAniState() {
        idleState = addAniState("Idle") { _, build ->
            Draw.rect(idleTR, build.x, build.y)
        }
        WorkingState = addAniState("Working") { _, build ->
            WorkingAnimation.draw(build.x, build.y, build)
            Drawf.light(
                build.team, build.x, build.y,
                5f,
                Color.white,
                1f
            )
        }
    }

    override fun genAniConfig() {
        aniConfig = AniConfig()
        aniConfig.defaultState(idleState)
        aniConfig.entry(idleState, WorkingState) { _, build ->
            !Mathf.zero(build.progress)
        }
        aniConfig.entry(WorkingState, idleState) { _, build ->
            Mathf.zero(build.progress) && Mathf.zero(build.power.status)
        }
        aniConfig.build()
    }

    override fun load() {
        super.load()
        WorkingAnimation = this.autoAnim("indicator-light", WorkingAnimFrameNumber, WorkingAnimDuration)
        idleTR = this.subA("light-off")
    }
}