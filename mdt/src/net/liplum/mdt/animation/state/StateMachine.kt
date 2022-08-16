package net.liplum.mdt.animation.state

import arc.graphics.g2d.Draw
import mindustry.gen.Building
import net.liplum.CanRefresh
import net.liplum.mdt.WhenNotPaused
import net.liplum.mdt.render.RESET_CONTEXT
import plumy.core.arc.Tick

interface ISwitchStateListener<TBuild : Building> {
    fun onSwitch(build: TBuild, from: State<TBuild>, to: State<TBuild>)
}

open class StateMachine<TBuild : Building>(
    val config: StateConfig<TBuild>,
    val build: TBuild,
) {
    var transition: TransitionEffect = config.transition
    var transitionDuration: Float = config.transitionDuration
    var curState: State<TBuild> = config.defaultState!!
    var lastState: State<TBuild>? = null
    var switchAniStateListener: ISwitchStateListener<TBuild>? = null
    var onUpdate: (() -> Unit)? = null
        private set

    open fun onUpdate(onUpdate: () -> Unit): StateMachine<TBuild> {
        this.onUpdate = onUpdate
        return this
    }
    /**
     * For java
     */
    fun onUpdate(onUpdate: Runnable): StateMachine<TBuild> {
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
    open fun updateState() {
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

fun <T:Building> StateMachine<T>.update(delta:Tick){
    WhenNotPaused {
        spend(delta)
    }
    if (CanRefresh()) {
        updateState()
    }
}