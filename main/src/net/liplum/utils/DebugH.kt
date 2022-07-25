@file:JvmName("DebugH")

package net.liplum.utils

import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.logic.Ranged
import mindustry.world.Block
import mindustry.world.blocks.production.GenericCrafter
import net.liplum.R
import net.liplum.api.brain.IUpgradeComponent
import net.liplum.api.cyber.*
import net.liplum.common.utils.*
import net.liplum.mdt.animations.anis.IAniSMedBuild
import net.liplum.mdt.ui.bars.AddBar

fun Block.addTeamInfo() {
    AddBar<Building>(R.Bar.TeamN,
        { R.Bar.Team.bundle(team) },
        { Pal.powerBar },
        { 1f }
    )
}

inline fun <reified T> Block.addRangeInfo(maxRange: Float) where T : Building, T : Ranged {
    AddBar<T>(R.Bar.RangeN,
        { R.Bar.Range.bundle((range() / Vars.tilesize).format(1)) },
        { Pal.range },
        { range() / maxRange }
    )
}

inline fun <reified T> Block.addAniStateInfo() where T : Building, T : IAniSMedBuild<*, *> {
    AddBar<T>(R.Bar.AniStateN,
        { aniStateM.curState.stateName },
        { Pal.bar },
        { 1f }
    )
    AddBar<T>(R.Bar.AniStateLastN,
        {
            R.Bar.AniStateLast.bundle(
                aniStateM.lastState?.stateName ?: R.Bar.Null.bundle()
            )
        },
        { Pal.bar },
        { (aniStateM.lastState != null).Float }
    )
}

inline fun <reified T> Block.addProgressInfo() where T : GenericCrafter.GenericCrafterBuild {
    AddBar<T>(R.Bar.ProgressN,
        { R.Bar.Progress.bundle(progress.percentI) },
        { Pal.power },
        { progress / 1f }
    )
}

inline fun <reified T> Block.addReceiverInfo() where T : Building, T : IDataSender {
    AddBar<T>(R.Bar.ReceiverN,
        {
            R.Bar.Receiver.bundle(connectedReceivers.size)
        },
        { R.C.Receiver },
        {
            var max = maxReceiverConnection
            if (max == -1) {
                max = 10
            }
            connectedReceivers.size.toFloat() / max
        }
    )
}

inline fun <reified T> Block.addSenderInfo() where T : Building, T : IDataReceiver {
    AddBar<T>(R.Bar.SenderN,
        { R.Bar.Sender.bundle(connectedSenders.size) },
        { R.C.Sender },
        {
            var max = maxSenderConnection
            if (max == -1) {
                max = 10
            }
            connectedSenders.size.toFloat() / max
        }
    )
}

inline fun <reified T> Block.addClientInfo() where T : Building, T : IStreamHost {
    AddBar<T>(R.Bar.ClientN,
        { R.Bar.Client.bundle(connectedClients.size) },
        { R.C.Client },
        {
            var max = maxClientConnection
            if (max == -1) {
                max = 10
            }
            connectedClients.size.toFloat() / max
        }
    )
}

inline fun <reified T> Block.addHostInfo() where T : Building, T : IStreamClient {
    AddBar<T>(R.Bar.HostN,
        { R.Bar.Host.bundle(connectedHosts.size) },
        { R.C.Host },
        {
            var max = maxHostConnection
            if (max == -1) {
                max = 10
            }
            connectedHosts.size.toFloat()/ max
        }
    )
}

inline fun <reified T> Block.addBrainInfo() where T : Building, T : IUpgradeComponent {
    AddBar<T>(R.Bar.LinkedN,
        { R.Bar.Linked.bundle(isLinkedBrain) },
        { R.C.Host },
        { isLinkedBrain.toFloat() }
    )
}

inline fun <reified T> Block.addSendingProgress() where T : Building, T : INetworkNode {
    AddBar<T>(R.Bar.ProgressN,
        { R.Bar.Progress.bundle(sendingProgress) },
        { R.C.Power },
        { sendingProgress / 1f }
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