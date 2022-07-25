@file:JvmName("CyberH")

package net.liplum.api.cyber

import arc.graphics.Color
import arc.math.geom.Point2
import arc.struct.Seq
import arc.util.Time
import mindustry.Vars
import mindustry.type.Item
import mindustry.type.Liquid
import net.liplum.R
import net.liplum.Var
import net.liplum.annotations.SubscribeEvent
import net.liplum.api.ICyberEntity
import net.liplum.common.Changed
import net.liplum.common.utils.Or
import net.liplum.common.utils.isLineInViewField
import net.liplum.events.CioInitEvent
import net.liplum.lib.math.Point2f
import net.liplum.lib.math.smooth
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.*

private val p1 = Point2f()
private val p2 = Point2f()
private val c1 = Color()
private val c2 = Color()
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
object DataCenter {
    @JvmField var SingleItems: Array<SingleItemArray> = emptyArray()
    @JvmStatic
    @SubscribeEvent(CioInitEvent::class)
    fun initData() {
        val items = Vars.content.items()
        SingleItems = Array(items.size) {
            Seq.with(items[it])
        }
    }
}

val EmptySingleItemArray: SingleItemArray = Seq()
val Item?.req: SingleItemArray
    get() = if (this == null)
        EmptySingleItemArray
    else
        DataCenter.SingleItems[this.ID]

fun Item?.match(requirements: SingleItemArray?): Boolean {
    this ?: return false
    requirements ?: return true
    return this in requirements
}

fun Int.isAccepted() =
    this == -1 || this > 0


typealias SingleLiquidArray = Seq<Liquid>

object StreamCenter {
    @JvmField var SingleLiquid: Array<SingleLiquidArray> = emptyArray()
    @JvmStatic
    fun initStream() {
        val liquids = Vars.content.liquids()
        SingleLiquid = Array(liquids.size) {
            Seq.with(liquids[it])
        }
    }
    @JvmStatic
    @ClientOnly
    fun loadLiquidsColor() {
        val liquids = Vars.content.liquids()
        R.C.LiquidColors = Array(liquids.size) {
            val liquid = liquids[it]
            val color = liquid.color
            // To prevent crash with the adaptor of liquid because their Liquid#color is null. @Discord Normalperson666#2826
            // So I just cause a crash when loading and provide more details for handling with it.
            assert(color != null) {
                "${liquid.localizedName}(${liquid.name} of ${liquid.javaClass.name}) in ${liquid.minfo?.mod} has a nullable color." +
                        "This message is only for notifying that you might be on a extreme condition about mods or some mods doesn't obey the rules of Mindustry." +
                        "Try again after uninstalling some unnecessary mods."
            }
            color
        }
    }
    @JvmStatic
    @ClientOnly
    fun initStreamColors() {
        R.C.HostLiquidColors = Array(R.C.LiquidColors.size) {
            R.C.LiquidColors[it].cpy().lerp(R.C.Host, 0.4f)
        }
        R.C.ClientLiquidColors = Array(R.C.LiquidColors.size) {
            R.C.LiquidColors[it].cpy().lerp(R.C.Client, 0.4f)
        }
    }
    @JvmStatic
    @SubscribeEvent(CioInitEvent::class)
    fun initAndLoad() {
        initStream()
        ClientOnly {
            loadLiquidsColor()
            initStreamColors()
        }
    }
}

val EmptySingleLiquidArray: SingleLiquidArray = Seq()
val Liquid?.req: SingleLiquidArray
    get() = if (this == null)
        EmptySingleLiquidArray
    else
        StreamCenter.SingleLiquid[this.ID]

fun Liquid?.match(requirements: SingleLiquidArray?): Boolean {
    this ?: return false
    requirements ?: return true
    return this in requirements
}
@ClientOnly
val Liquid?.hostColor: Color
    get() = if (this == null)
        R.C.Host
    else
        R.C.HostLiquidColors[this.ID]
@ClientOnly
val Liquid?.clientColor: Color
    get() = if (this == null)
        R.C.Client
    else
        R.C.ClientLiquidColors[this.ID]

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
fun isConfiguringSender(): Boolean {
    val selected = Vars.control.input.config.selected
    return selected is IDataSender
}

fun isConfiguringHost(): Boolean {
    val selected = Vars.control.input.config.selected
    return selected is IStreamHost
}
//</editor-fold>
//<editor-fold desc="Check Connection">
fun IDataReceiver.checkSendersPos() {
    connectedSenders.removeAll { !it.ds().exists }
}

fun IDataSender.checkReceiversPos() {
    connectedReceivers.removeAll { !it.dr().exists }
}

fun IStreamHost.checkClientsPos() {
    connectedClients.removeAll { !it.sc().exists }
}

fun IStreamClient.checkHostsPos() {
    connectedHosts.removeAll { !it.sh().exists }
}
//</editor-fold>
