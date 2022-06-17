package net.liplum.blocks.icmachine

import arc.func.Prov
import mindustry.graphics.Pal
import mindustry.type.Item
import net.liplum.DebugOnly
import net.liplum.lib.assets.TR
import net.liplum.blocks.AniedCrafter
import net.liplum.common.utils.percentI
import net.liplum.lib.math.FUNC
import net.liplum.lib.math.isZero
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.animations.anis.AniState
import net.liplum.mdt.animations.anis.None
import net.liplum.mdt.animations.anis.config
import net.liplum.mdt.render.Draw
import net.liplum.mdt.render.SetAlpha
import net.liplum.mdt.ui.bars.AddBar
import net.liplum.mdt.utils.sub
import kotlin.math.sqrt

private typealias AniStateMS = AniState<ICMachineS, ICMachineS.ICMachineSBuild>

private val P2ALeft = (3 - sqrt(3f)) / 6
private val P2ARight = (3 + sqrt(3f)) / 6
private val P2A: FUNC = {
    if (it in P2ALeft..P2ARight)
        1f
    else
        -6 * it * it + 6 * it
}

open class ICMachineS(name: String) : AniedCrafter<ICMachineS, ICMachineS.ICMachineSBuild>(name) {
    @ClientOnly var phase = 3
    @ClientOnly lateinit var Baffle: TR
    @ClientOnly lateinit var processIcons: Array<Item>
    @ClientOnly @JvmField var baffleMinAlpha = 0.65f
    @ClientOnly @JvmField var baffleMaxAlpha = 1f

    init {
        buildType = Prov { ICMachineSBuild() }
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            AddBar<ICMachineSBuild>("alpha",
                { "alpha:${baffleAlpha.percentI}" },
                { Pal.powerBar },
                { baffleAlpha / 1f }
            )
        }
    }

    override fun load() {
        super.load()
        Baffle = this.sub("baffle")
    }

    override fun icons() = arrayOf(
        region, Baffle
    )

    open inner class ICMachineSBuild : AniedCrafter<ICMachineS, ICMachineSBuild>.AniedCrafterBuild() {
        @ClientOnly open var baffleAlpha = baffleMinAlpha
            set(value) {
                field = value.coerceIn(baffleMinAlpha, baffleMaxAlpha)
            }
        var processEffectShown = true
        override fun onAniStateMUpdate() {
            when (aniStateM.curState) {
                WorkingState -> {
                    baffleAlpha -= 0.01f * delta()
                }
                IdleState -> baffleAlpha += 0.01f * delta()
            }
        }

        override fun updateTile() {
            super.updateTile()
            if (progress % 0.1 < 0.01f) {
                if (!processEffectShown) {
                    craftEffect.at(this)
                    processEffectShown = true
                }
            } else {
                processEffectShown = false
            }
        }
    }

    @ClientOnly lateinit var IdleState: AniStateMS
    @ClientOnly lateinit var WorkingState: AniStateMS
    override fun genAniState() {
        IdleState = addAniState("Idle") {
            SetAlpha(baffleAlpha)
            Baffle.Draw(x, y)
        }
        WorkingState = addAniState("Working") {
            val animProgress = progress * phase
            val curIndex = animProgress.toInt().coerceIn(0, processIcons.size - 1)
            val curTR = processIcons[curIndex].fullIcon
            val progressInCurPeriod = progress % (1f / phase) / (1f / phase)
            SetAlpha(P2A(progressInCurPeriod))
            curTR.Draw(x, y)
            SetAlpha(baffleAlpha)
            Baffle.Draw(x, y)
        }
    }

    override fun genAniConfig() {
        config {
            transition(None)
            From(IdleState) To WorkingState When {
                !progress.isZero && !power.status.isZero
            }
            From(WorkingState) To IdleState When {
                progress.isZero || power.status.isZero
            }
        }
    }
}