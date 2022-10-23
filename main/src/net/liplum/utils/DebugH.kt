@file:JvmName("DebugH")

package net.liplum.utils

import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.logic.Ranged
import mindustry.world.Block
import net.liplum.R
import net.liplum.common.util.Float
import plumy.dsl.bundle
import net.liplum.common.util.format
import net.liplum.common.util.percentI
import plumy.animation.state.IStateful
import plumy.dsl.AddBar

inline fun <reified T> Block.addRangeInfo(maxRange: Float) where T : Building, T : Ranged {
    AddBar<T>(R.Bar.RangeN,
        { R.Bar.Range.bundle((range() / Vars.tilesize).format(1)) },
        { Pal.range },
        { range() / maxRange }
    )
}

inline fun <reified T> Block.addStateMachineInfo()
        where T : Building, T : IStateful<*> {
    AddBar<T>("state-machine",
        { stateMachine.curState.stateName },
        { Pal.bar },
        { 1f }
    )
    AddBar<T>("state-machine-last",
        {
            "Last: ${stateMachine.lastState?.stateName ?: R.Bar.Null.bundle}"
        },
        { Pal.bar },
        { (stateMachine.lastState != null).Float }
    )
}

inline fun <reified T> Block.addProgressInfo() where T : Building {
    AddBar<T>("progress",
        { "${"bar.loadprogress".bundle}: ${progress().percentI}" },
        { Pal.power },
        { progress() }
    )
}

fun <T : UnlockableContent> Array<T>.genText(): String {
    val s = StringBuilder()
    s.append('[')
    s.append(this.size)
    s.append(']')
    for (req in this.sortedBy { it.id }) {
        s.append(req.localizedName)
        s.append(' ')
    }
    return s.toString()
}

fun <T : UnlockableContent> Iterable<T>.genText(): String {
    val s = StringBuilder()
    var count = 0
    for (req in this.sortedBy { it.id }) {
        s.append(req.localizedName)
        s.append(' ')
        count++
    }
    s.insert(0, "[$count]")
    return s.toString()
}