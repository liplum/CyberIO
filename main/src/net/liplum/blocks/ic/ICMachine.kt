package net.liplum.blocks.ic

import arc.func.Prov
import arc.util.Time
import net.liplum.blocks.AniedCrafter
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.animation.AnimationMeta
import net.liplum.mdt.animation.anis.AniState
import net.liplum.mdt.animation.anis.configStates
import net.liplum.mdt.animation.draw
import net.liplum.mdt.render.SetAlpha
import net.liplum.mdt.utils.animationMeta
import net.liplum.mdt.utils.instantiateSideOnly
import plumy.core.math.FUNC
import plumy.core.math.isZero

private typealias AniStateM = AniState<ICMachine, ICMachine.ICMachineBuild>

private const val workingAnimAlphaA = 0.1f / (0.03f * 0.03f)

open class ICMachine(name: String) : AniedCrafter<ICMachine, ICMachine.ICMachineBuild>(name) {
    @ClientOnly lateinit var IdleState: AniStateM
    @ClientOnly lateinit var WorkingState: AniStateM
    @ClientOnly var WorkingAnim = AnimationMeta.Empty
    @ClientOnly @JvmField var WorkingAnimFrameNumber = 4
    @ClientOnly @JvmField var WorkingAnimDuration = 120f
    var workingAnimAlpha: FUNC = { workingAnimAlphaA * it * it }

    init {
        hasPower = true
        buildType = Prov { ICMachineBuild() }
    }

    override fun load() {
        super.load()
        WorkingAnim = this.animationMeta("indicator-light", WorkingAnimFrameNumber, WorkingAnimDuration)
    }

    open inner class ICMachineBuild : AniedCrafter<ICMachine, ICMachineBuild>.AniedCrafterBuild() {
        var workingAnimObj = WorkingAnim.instantiateSideOnly()

        init {
            ClientOnly {
                /*workingAnimObj.tmod {
                    it * (0.1f + Mathf.lerp(
                        progress, progress * 8, 0.5f
                    ))
                }*/
            }
        }

        override fun beforeDraw() {
            workingAnimObj.spend(Time.delta)
        }
    }

    override fun genAniState() {
        IdleState = addAniState("Idle")
        WorkingState = addAniState("Working") {
            SetAlpha(workingAnimAlpha(progress))
            workingAnimObj.draw(x, y)
        }
    }

    override fun genAniConfig() {
        configStates {
            From(IdleState) To WorkingState When {
                !progress.isZero && !power.status.isZero
            }
            From(WorkingState) To IdleState When {
                progress.isZero || power.status.isZero
            }
        }
    }
}