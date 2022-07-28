@file:JvmName("DebugH")

package net.liplum.util

import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.logic.Ranged
import mindustry.world.Block
import net.liplum.R
import net.liplum.api.brain.IUpgradeComponent
import net.liplum.api.cyber.INetworkNode
import net.liplum.common.util.*
import net.liplum.mdt.animation.anis.IAniSMedBuild
import net.liplum.mdt.ui.bars.AddBar

inline fun <reified T> Block.addRangeInfo(maxRange: Float) where T : Building, T : Ranged {
    AddBar<T>(R.Bar.RangeN,
        { R.Bar.Range.bundle((range() / Vars.tilesize).format(1)) },
        { Pal.range },
        { range() / maxRange }
    )
}

inline fun <reified T> Block.addAniStateInfo() where T : Building, T : IAniSMedBuild<*, *> {
    AddBar<T>("ani-state",
        { aniStateM.curState.stateName },
        { Pal.bar },
        { 1f }
    )
    AddBar<T>("ani-state-last",
        {
            "Last: ${aniStateM.lastState?.stateName ?: R.Bar.Null.bundle}"
        },
        { Pal.bar },
        { (aniStateM.lastState != null).Float }
    )
}

inline fun <reified T> Block.addProgressInfo() where T : Building {
    AddBar<T>("progress",
        { "${"bar.loadprogress".bundle}: ${progress().percentI}" },
        { Pal.power },
        { progress() }
    )
}

inline fun <reified T> Block.addSendingProgress() where T : Building, T : INetworkNode {
    AddBar<T>("sending-progress",
        { "Sending: $sendingProgress" },
        { R.C.Power },
        { sendingProgress }
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