@file:JvmName("DebugH")

package net.liplum.utils

import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.logic.Ranged
import mindustry.ui.Bar
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.meta.BlockBars
import net.liplum.R
import net.liplum.animations.anis.IAniSMedBuild
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender

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
            { R.Bar.AniStateLast.bundle(it.aniStateM.lastState?.stateName ?: "") },
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