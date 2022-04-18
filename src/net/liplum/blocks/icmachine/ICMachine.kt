package net.liplum.blocks.icmachine

import arc.math.Mathf
import arc.util.Time
import net.liplum.ClientOnly
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.animations.anims.AnimationObj
import net.liplum.lib.animations.anis.AniState
import net.liplum.lib.SetAlpha
import net.liplum.lib.animations.anis.config
import net.liplum.blocks.AniedCrafter
import net.liplum.utils.FUNC
import net.liplum.utils.autoAnim
import net.liplum.utils.isZero

private typealias AniStateM = AniState<ICMachine, ICMachine.ICMachineBuild>

private const val workingAnimAlphaA = 0.1f / (0.03f * 0.03f)

open class ICMachine(name: String) : AniedCrafter<ICMachine, ICMachine.ICMachineBuild>(name) {
    @ClientOnly lateinit var IdleState: AniStateM
    @ClientOnly lateinit var WorkingState: AniStateM
    @ClientOnly lateinit var WorkingAnim: Animation
    @ClientOnly @JvmField var WorkingAnimFrameNumber = 4
    @ClientOnly @JvmField var WorkingAnimDuration = 120f
    var workingAnimAlpha: FUNC = { workingAnimAlphaA * it * it }

    init {
        hasPower = true
    }

    override fun load() {
        super.load()
        WorkingAnim = this.autoAnim("indicator-light", WorkingAnimFrameNumber, WorkingAnimDuration)
    }

    open inner class ICMachineBuild : AniedCrafter<ICMachine, ICMachineBuild>.AniedCrafterBuild() {
        lateinit var workingAnimObj: AnimationObj

        init {
            ClientOnly {
                workingAnimObj = WorkingAnim.gen()
                workingAnimObj.tmod {
                    it * (0.1f + Mathf.lerp(
                        progress, progress * 8, 0.5f
                    ))
                }
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
        config {
            From(IdleState) To WorkingState When {
                !progress.isZero && !power.status.isZero
            }
            From(WorkingState) To IdleState When {
                progress.isZero || power.status.isZero
            }
        }
    }
}