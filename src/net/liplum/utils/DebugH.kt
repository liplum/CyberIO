@file:JvmName("DebugH")

package net.liplum.utils

import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.logic.Ranged
import mindustry.ui.Bar
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.meta.BlockBars
import net.liplum.R
import net.liplum.animations.anis.IAniSMedBuild
import net.liplum.api.cyber.IDataReceiver
import net.liplum.api.cyber.IDataSender
import net.liplum.api.cyber.IStreamClient
import net.liplum.api.cyber.IStreamHost

annotation class CioDebugOnly

fun BlockBars.addTeamInfo() {
    this.add<Building>(
        R.Bar.TeamN
    ) {
        Bar(
            { R.Bar.Team.bundle(it.team) },
            { Pal.powerBar },
            { 1f }
        )
    }
}

fun <T> BlockBars.addRangeInfo(maxRange: Float) where T : Building, T : Ranged {
    this.add<T>(
        R.Bar.RangeN
    ) {
        Bar(
            { R.Bar.Range.bundle((it.range() / Vars.tilesize).format(1)) },
            { Pal.range },
            { it.range() / maxRange }
        )
    }
}

fun <T> BlockBars.addAniStateInfo() where T : Building, T : IAniSMedBuild<*, *> {
    this.add<T>(
        R.Bar.AniStateN
    ) {
        Bar(
            { it.aniStateM.curState.stateName },
            { Pal.bar },
            { 1f }
        )
    }
    this.add<T>(
        R.Bar.AniStateLastN
    ) {
        Bar(
            {
                R.Bar.AniStateLast.bundle(
                    it.aniStateM.lastState?.stateName ?: R.Bar.Null.bundle()
                )
            },
            { Pal.bar },
            { (it.aniStateM.lastState != null).Float }
        )
    }
}

fun BlockBars.addSleepInfo() {
    this.add<Building>(
        R.Bar.IsAsleepN
    ) {
        Bar(
            { R.Bar.IsAsleep.bundle(it.sleeping.yesNo()) },
            { Pal.power },
            { (it.sleeping).Float }
        )
    }
}

fun <T> BlockBars.addProgressInfo() where T : GenericCrafter.GenericCrafterBuild {
    this.add<T>(
        R.Bar.ProgressN
    ) {
        Bar(
            { R.Bar.Progress.bundle(it.progress.percentI) },
            { Pal.power },
            { it.progress / 1f }
        )
    }
}

fun <T> BlockBars.addReceiverInfo() where T : Building, T : IDataSender {
    this.add<T>(
        R.Bar.ReceiverN
    ) {
        Bar(
            {
                val connected = if (it.canMultipleConnect())
                    it.connectedReceivers().size
                else
                    if (it.connectedReceiver() != null) 1 else 0
                R.Bar.Receiver.bundle(connected)
            },
            { R.C.Receiver },
            {
                val connected = if (it.canMultipleConnect())
                    it.connectedReceivers().size
                else
                    if (it.connectedReceiver() != null) 1 else 0
                var max = it.maxReceiverConnection()
                if (max == -1) {
                    max = 10
                }
                connected.toFloat() / max
            }
        )
    }
}

fun <T> BlockBars.addSenderInfo() where T : Building, T : IDataReceiver {
    this.add<T>(
        R.Bar.SenderN
    ) {
        Bar(
            { R.Bar.Sender.bundle(it.connectedSenders().size) },
            { R.C.Sender },
            {
                var max = it.maxSenderConnection()
                if (max == -1) {
                    max = 10
                }
                it.connectedSenders().size.toFloat() / max
            }
        )
    }
}

fun <T> BlockBars.addClientInfo() where T : Building, T : IStreamHost {
    this.add<T>(
        R.Bar.ClientN
    ) {
        Bar(
            { R.Bar.Client.bundle(it.connectedClients().size) },
            { R.C.Client },
            {
                var max = it.maxClientConnection()
                if (max == -1) {
                    max = 10
                }
                it.connectedClients().size.toFloat() / max
            }
        )
    }
}

fun <T> BlockBars.addHostInfo() where T : Building, T : IStreamClient {
    this.add<T>(
        R.Bar.HostN
    ) {
        Bar(
            { R.Bar.Host.bundle(it.connectedHosts().size) },
            { R.C.Host },
            {
                var max = it.maxHostConnection()
                if (max == -1) {
                    max = 10
                }
                it.connectedHosts().size.toFloat() / max
            }
        )
    }
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