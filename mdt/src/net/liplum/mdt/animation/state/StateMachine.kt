package net.liplum.mdt.animation.state

import arc.graphics.g2d.Draw
import net.liplum.mdt.animation.ITimer
import net.liplum.mdt.animation.ContextDraw.RESET_CONTEXT

typealias SwitchStateEvent<T> = (e: T, from: State<T>, to: State<T>) -> Unit

interface IStateful<T> {
    val stateMachine: StateMachine<T>
}

open class StateMachine<T>(
    val config: StateConfig<T>,
    val entity: T,
) : ITimer {
    var transition: TransitionEffect = config.transition
    var transitionDuration: Float = config.transitionDuration
    var curState: State<T> = config.defaultState ?: throw IllegalArgumentException("StateConfig's defaultState is null.")
    var lastState: State<T>? = null
    var onStateSwitched: SwitchStateEvent<T>? = null
    var onUpdateState: (() -> Unit)? = null
    var lastSwitchTime = 0f
    var curTime = 0f
    override fun spend(time: Float) {
        curTime += time
    }

    open fun draw() {
        val delta = curTime - lastSwitchTime
        val progress = (delta / transitionDuration).coerceIn(0f, 1f)
        transition(progress, {
            lastState?.drawBuilding(entity)
        }, {
            curState.drawBuilding(entity)
        })
        Draw.reset()
        RESET_CONTEXT()
    }

    open fun curOverwriteBlock(): Boolean = curState.isOverwriteBlock
    open fun updateState() {
        onUpdateState?.invoke()
        for (to in config.getAllEntrances(curState)) {
            val canEnter = config.getEntranceCondition(curState, to)
            if (canEnter != null && canEnter(entity)) {
                onStateSwitched?.invoke(entity, curState, to)
                lastState = curState
                curState = to
                lastSwitchTime = curTime
                return
            }
        }
    }
}

fun <T> StateMachine<T>.onUpdate(onUpdate: () -> Unit): StateMachine<T> {
    this.onUpdateState = onUpdate
    return this
}

fun <T> StateMachine<T>.onUpdate(onUpdate: Runnable): StateMachine<T> {
    this.onUpdateState = {
        onUpdate.run()
    }
    return this
}