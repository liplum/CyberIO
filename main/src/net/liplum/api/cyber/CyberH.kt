@file:JvmName("CyberH")

package net.liplum.api.cyber

import arc.graphics.Color
import arc.math.geom.Point2
import arc.struct.Seq
import arc.util.Time
import mindustry.Vars
import mindustry.gen.Building
import mindustry.type.Item
import mindustry.type.Liquid
import mindustry.world.Block
import net.liplum.R
import net.liplum.Var
import net.liplum.annotations.SubscribeEvent
import net.liplum.api.ICyberEntity
import net.liplum.common.Changed
import net.liplum.common.util.Or
import net.liplum.common.util.bundle
import net.liplum.common.util.toFloat
import net.liplum.event.CioInitEvent
import net.liplum.lib.math.Point2f
import net.liplum.lib.math.smooth
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.ui.bars.AddBar
import net.liplum.mdt.utils.*

private val p1 = Point2f()
private val p2 = Point2f()
private val c1 = Color()
private val c2 = Color()
val ICyberEntity.isFlying: Boolean
    get() = tile == Vars.emptyTile
//<editor-fold desc="Try Cast">
fun Int.dr(): IDataReceiver? =
    this.build as? IDataReceiver

fun Int.drOrPayload(): IDataReceiver? =
    this.dr() Or { this.inPayload() }

fun Int.ds(): IDataSender? =
    this.build as? IDataSender

fun Int.dsOrPayload(): IDataSender? =
    this.ds() Or { this.inPayload() }

fun Point2?.dr(): IDataReceiver? =
    this?.let { this.build as? IDataReceiver }

fun Point2?.drOrPayload(): IDataReceiver? =
    this?.let { this.dr() Or { this.inPayload() } }

fun Point2?.ds(): IDataSender? =
    this?.let { this.build as? IDataSender }

fun Point2?.dsOrPayload(): IDataSender? =
    this?.let { this.ds() Or { this.inPayload() } }

fun Int.sc(): IStreamClient? =
    this.build as? IStreamClient

fun Int.sh(): IStreamHost? =
    this.build as? IStreamHost

fun Point2?.sc(): IStreamClient? =
    this?.let { this.build as? IStreamClient }

fun Point2?.sh(): IStreamHost? =
    this?.let { this.build as? IStreamHost }

fun Int.nn(): INetworkNode? =
    this.build as? INetworkNode

fun Int.p2p(): IP2pNode? =
    this.build as? IP2pNode

fun Point2?.p2p(): IP2pNode? =
    this?.let { this.build as? IP2pNode }
//</editor-fold>
val ICyberEntity?.exists: Boolean
    get() = this != null && this.building.exists
//<editor-fold desc="Tile Position">
val ICyberEntity.bottomLeftX: Int
    get() = building.bottomLeftX
val ICyberEntity.bottomLeftY: Int
    get() = building.bottomLeftY
val ICyberEntity.bottomRightX: Int
    get() = building.bottomRightX
val ICyberEntity.bottomRightY: Int
    get() = building.bottomRightY
val ICyberEntity.topLeftX: Int
    get() = building.topLeftX
val ICyberEntity.topLeftY: Int
    get() = building.topLeftY
val ICyberEntity.topRightX: Int
    get() = building.topRightX
val ICyberEntity.topRightY: Int
    get() = building.topRightY
val ICyberEntity?.tileX: Int
    get() = (this?.tile?.x ?: 0).toInt()
val ICyberEntity?.tileY: Int
    get() = (this?.tile?.y ?: 0).toInt()
val ICyberEntity?.tileXd: Double
    get() = (this?.tile?.x ?: 0).toDouble()
val ICyberEntity?.tileYd: Double
    get() = (this?.tile?.y ?: 0).toDouble()
//</editor-fold>
typealias SingleItemArray = Seq<Item>

object CyberDataLoader {
    @JvmField var SingleItems: Array<SingleItemArray> = emptyArray()
    @JvmField var SingleLiquid: Array<SingleLiquidArray> = emptyArray()
    @SubscribeEvent(CioInitEvent::class)
    fun initData() {
        val items = Vars.content.items()
        SingleItems = Array(items.size) {
            Seq.with(items[it])
        }
        val liquids = Vars.content.liquids()
        SingleLiquid = Array(liquids.size) {
            Seq.with(liquids[it])
        }
    }
}

val EmptySingleItemArray: SingleItemArray = Seq()
val Item?.req: SingleItemArray
    get() = if (this == null)
        EmptySingleItemArray
    else
        CyberDataLoader.SingleItems[this.ID]

fun Item?.match(requirements: SingleItemArray?): Boolean {
    this ?: return false
    requirements ?: return true
    return this in requirements
}

fun Int.isAccepted() =
    this == -1 || this > 0
typealias SingleLiquidArray = Seq<Liquid>

val EmptySingleLiquidArray: SingleLiquidArray = Seq()
val Liquid?.req: SingleLiquidArray
    get() = if (this == null)
        EmptySingleLiquidArray
    else
        CyberDataLoader.SingleLiquid[this.ID]

fun Liquid?.match(requirements: SingleLiquidArray?): Boolean {
    this ?: return false
    requirements ?: return true
    return this in requirements
}

fun transitionColor(from: Changed<Color>, to: Color): Color {
    val last = from.old
    return if (last != null) c1.set(last).lerp(
        to,
        ((Time.time - from.timestamp) / Var.RsColorTransitionTime).smooth
    ) else to
}

fun Float.isAccepted() =
    this <= -1f || this > 0f
//<editor-fold desc="Check if Configuring">
@ClientOnly
inline fun whenNotConfiguringSender(func: () -> Unit) {
    if (!isConfiguringSender()) {
        func()
    }
}
@ClientOnly
inline fun whenNotConfiguringHost(func: () -> Unit) {
    if (!isConfiguringHost()) {
        func()
    }
}
@ClientOnly
inline fun whenNotConfiguringP2P(func: () -> Unit) {
    if (!isConfiguringP2P()) {
        func()
    }
}
@ClientOnly
fun isConfiguringSender(): Boolean {
    val selected = Vars.control.input.config.selected
    return selected is IDataSender
}
@ClientOnly
fun isConfiguringHost(): Boolean {
    val selected = Vars.control.input.config.selected
    return selected is IStreamHost
}

fun isConfiguringP2P(): Boolean {
    val selected = Vars.control.input.config.selected
    return selected is IP2pNode
}
//</editor-fold>
//<editor-fold desc="Check Connection">
fun IDataReceiver.checkSendersPos() =
    connectedSenders.removeAll { !it.ds().exists }

fun IDataSender.checkReceiversPos() =
    connectedReceivers.removeAll { !it.dr().exists }

fun IStreamHost.checkClientsPos() =
    connectedClients.removeAll { !it.sc().exists }

fun IStreamClient.checkHostsPos() =
    connectedHosts.removeAll { !it.sh().exists }

fun IP2pNode.checkConnection() =
    if (!connected.exists) {
        connectedPos = -1
        true
    } else false
//</editor-fold>
//<editor-fold desc="Bars">
inline fun <reified T> Block.addReceiverInfo() where T : Building, T : IDataSender {
    AddBar<T>(
        R.Bar.ReceiverN,
        {
            "${R.Bar.Receiver.bundle}: ${connectedReceivers.size}"
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
    AddBar<T>(
        R.Bar.SenderN,
        { "${R.Bar.Sender.bundle}: ${connectedSenders.size}" },
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
    AddBar<T>(
        R.Bar.ClientN,
        { "${R.Bar.Client.bundle}: ${connectedClients.size}" },
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
    AddBar<T>(
        R.Bar.HostN,
        { "${R.Bar.Host.bundle}: ${connectedHosts.size}" },
        { R.C.Host },
        {
            var max = maxHostConnection
            if (max == -1) {
                max = 10
            }
            connectedHosts.size.toFloat() / max
        }
    )
}

inline fun <reified T> Block.addP2pLinkInfo() where T : Building, T : IP2pNode {
    AddBar<T>(
        R.Bar.LinkedN,
        {
            if (isConnected) R.Bar.Linked.bundle
            else R.Bar.Unlinked.bundle
        },
        { R.C.P2P },
        { isConnected.toFloat() }
    )
}
//</editor-fold>
