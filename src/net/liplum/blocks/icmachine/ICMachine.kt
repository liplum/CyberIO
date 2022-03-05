package net.liplum.blocks.icmachine

import arc.math.Mathf
import arc.util.Time
import net.liplum.ClientOnly
import net.liplum.animations.anims.Animation
import net.liplum.animations.anims.AnimationObj
import net.liplum.animations.anis.AniConfig
import net.liplum.animations.anis.AniState
import net.liplum.blocks.AniedCrafter
import net.liplum.utils.autoAnim

private typealias AniStateM = AniState<ICMachine, ICMachine.ICMachineBuild>

open class ICMachine(name: String) : AniedCrafter<ICMachine, ICMachine.ICMachineBuild>(name) {
    lateinit var idleState: AniStateM
    lateinit var WorkingState: AniStateM
    lateinit var WorkingAnim: Animation
    @JvmField var WorkingAnimFrameNumber = 4
    @JvmField var WorkingAnimDuration = 120f

    init {
        hasPower = true
    }

    override fun genAniState() {
        idleState = addAniState("Idle")
        WorkingState = addAniState("Working") { _, build ->
            build.workingAnimObj.draw(build.x, build.y)
        }
    }

    override fun genAniConfig() {
        aniConfig = AniConfig<ICMachine, ICMachineBuild>().apply {
            defaultState(idleState)
            From(idleState) To WorkingState When { _, build ->
                !Mathf.zero(build.progress) && !Mathf.zero(build.power.status)
            }
            From(WorkingState) To idleState When { _, build ->
                Mathf.zero(build.progress) || Mathf.zero(build.power.status)
            }
            build()
        }
    }

    override fun load() {
        super.load()
        WorkingAnim = this.autoAnim("indicator-light", WorkingAnimFrameNumber, WorkingAnimDuration)
    }

    open inner class ICMachineBuild : AniedCrafter<ICMachine, ICMachineBuild>.AniedCrafterBuild() {
        lateinit var workingAnimObj: AnimationObj
        override fun created() {
            super.created()
            ClientOnly {
                workingAnimObj = WorkingAnim.gen()
                workingAnimObj.tmod {
                    it * (0.1f + Mathf.lerp(
                        progress, progress * 8, 0.5f
                    ))
                }
            }
        }

        override fun draw() {
            workingAnimObj.spend(Time.delta)
            super.draw()
        }
    }
}