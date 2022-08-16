package net.liplum.mdt.animation.anis

import arc.graphics.g2d.Draw
import mindustry.gen.Building
import mindustry.world.Block
import net.liplum.mdt.render.RESET_CONTEXT

interface ISwitchAniStateListener<TBuild : Building> {
    fun onSwitch(build: TBuild, from: AniState<TBuild>, to: AniState<TBuild>)
}

open class AniStateM<TBuild : Building>(
    val config: AniConfig<TBuild>,
    val build: TBuild,
) {
    var transition: TransitionEffect = config.transition
    var transitionDuration: Float = config.transitionDuration
    var curState: AniState<TBuild> = config.defaultState!!
    var lastState: AniState<TBuild>? = null
    var switchAniStateListener: ISwitchAniStateListener<TBuild>? = null
    var onUpdate: (() -> Unit)? = null
        private set

    open fun onUpdate(onUpdate: () -> Unit): AniStateM<TBuild> {
        this.onUpdate = onUpdate
        return this
    }
    /**
     * For java
     */
    fun onUpdate(onUpdate: Runnable): AniStateM<TBuild> {
        onUpdate {
            onUpdate.run()
        }
        return this
    }

    var lastSwitchTime = 0f
    var curTime = 0f
    open fun spend(time: Float) {
        curTime += time
    }

    open fun drawBuilding() {
        val delta = curTime - lastSwitchTime
        val progress = (delta / transitionDuration).coerceIn(0f, 1f)
        transition(progress, {
            lastState?.drawBuilding(build)
        }, {
            curState.drawBuilding(build)
        })
        Draw.reset()
        RESET_CONTEXT()
    }

    open fun curOverwriteBlock(): Boolean = curState.isOverwriteBlock
    open fun update() {
        onUpdate?.invoke()
        for (to in config.getAllEntrances(curState)) {
            val canEnter = config.getCanEnter(curState, to)
            if (canEnter != null && canEnter(build)) {
                switchAniStateListener?.onSwitch(build, curState, to)
                lastState = curState
                curState = to
                lastSwitchTime = curTime
                return
            }
        }
    }
}