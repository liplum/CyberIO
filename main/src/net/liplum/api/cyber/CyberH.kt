@file:JvmName("CyberH")

package net.liplum.api.cyber

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.geom.Point2
import arc.util.Align
import mindustry.Vars
import mindustry.gen.Building
import mindustry.type.Item
import mindustry.type.Liquid
import mindustry.world.Block
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.Settings
import net.liplum.lib.bundle
import net.liplum.render.G
import net.liplum.render.Text
import net.liplum.render.Toaster
import net.liplum.render.fadeInOutPct
import net.liplum.utils.*

@ClientOnly
val ArrowDensity: Float
    get() = Settings.LinkArrowDensity
var ToastTimeFadePercent = 0.1f
var ToastTime = 180f
fun Int.db(): IDataBuilding? =
    this.build as? IDataBuilding

fun Int.dr(): IDataReceiver? =
    this.build as? IDataReceiver

fun Int.drOrPayload(): IDataReceiver? =
    this.dr() Or { this.inPayload() }

fun Int.ds(): IDataSender? =
    this.build as? IDataSender

fun Int.dsOrPayload(): IDataSender? =
    this.ds() Or { this.inPayload() }

fun Point2?.db(): IDataBuilding? =
    this?.let { this.build as? IDataBuilding }

fun Point2?.dr(): IDataReceiver? =
    this?.let { this.build as? IDataReceiver }

fun Point2?.drOrPayload(): IDataReceiver? =
    this?.let { this.dr() Or { this.inPayload() } }

fun Point2?.ds(): IDataSender? =
    this?.let { this.build as? IDataSender }

fun Point2?.dsOrPayload(): IDataSender? =
    this?.let { this.ds() Or { this.inPayload() } }

val IDataBuilding?.exists: Boolean
    get() = this != null && this.building.exists
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

typealias SingleItemArray = Array<Item>

object DataCenter {
    @JvmField var SingleItems: Array<SingleItemArray> = emptyArray()
    @JvmStatic
    fun initData() {
        val items = Vars.content.items()
        SingleItems = Array(items.size) {
            arrayOf(items[it])
        }
    }
}

val EmptySingleItemArray: SingleItemArray = emptyArray()
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

val ICyberEntity?.tileX: Int
    get() = (this?.tile?.x ?: 0).toInt()
val ICyberEntity?.tileY: Int
    get() = (this?.tile?.y ?: 0).toInt()
val ICyberEntity?.tileXd: Double
    get() = (this?.tile?.x ?: 0).toDouble()
val ICyberEntity?.tileYd: Double
    get() = (this?.tile?.y ?: 0).toDouble()

fun Int.sn(): IStreamNode? =
    this.build as? IStreamNode

fun Int.sc(): IStreamClient? =
    this.build as? IStreamClient

fun Int.sh(): IStreamHost? =
    this.build as? IStreamHost

fun Point2?.sn(): IStreamNode? =
    this?.let { this.build as? IStreamNode }

fun Point2?.sc(): IStreamClient? =
    this?.let { this.build as? IStreamClient }

fun Point2?.sh(): IStreamHost? =
    this?.let { this.build as? IStreamHost }

val IStreamNode?.exists: Boolean
    get() = this != null && this.building.exists

typealias SingleLiquidArray = Array<Liquid>

object StreamCenter {
    @JvmField var SingleLiquid: Array<SingleLiquidArray> = emptyArray()
    @JvmStatic
    fun initStream() {
        val liquids = Vars.content.liquids()
        SingleLiquid = Array(liquids.size) {
            arrayOf(liquids[it])
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
    fun initAndLoad() {
        initStream()
        ClientOnly {
            loadLiquidsColor()
            initStreamColors()
        }
    }
}

val EmptySingleLiquidArray: SingleLiquidArray = emptyArray()
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

fun Float.isAccepted() =
    this <= -1f || this > 0f
@JvmOverloads
@ClientOnly
fun IDataSender.drawDataNetGraphic(showCircle: Boolean = true) {
    if (receiverConnectionNumber <= 0) return
    if (showCircle) {
        G.drawSurroundingCircle(tile, R.C.Sender, alpha = Settings.LinkOpacity)
    }
    if (canMultipleConnect()) {
        this.drawReceivers(connectedReceivers, showCircle)
    } else {
        this.drawReceiver(connectedReceiver, showCircle)
    }
}
@JvmOverloads
@ClientOnly
fun IDataReceiver.drawDataNetGraphic(showCircle: Boolean = true) {
    if (senderConnectionNumber <= 0) return
    if (showCircle) {
        G.drawSurroundingCircle(tile, R.C.Receiver, alpha = Settings.LinkOpacity)
    }
    this.drawSenders(connectedSenders, showCircle)
}
@ClientOnly
fun IDataReceiver.drawRequirements() {
    val reqs = this.requirements
    if (reqs != null) {
        G.drawMaterialIcons(this.building, reqs, Settings.LinkOpacity * 0.8f)
    }
}
/**
 * Called in an [IDataReceiver] block
 *
 * @param x        tile x
 * @param y        tile y
 */
@ClientOnly
fun Block.drawLinkedLineToReceiverWhenConfiguring(x: Int, y: Int) {
    if (!Vars.control.input.config.isShown) return
    val selected = Vars.control.input.config.selected
    if (selected !is IDataSender) {
        return
    }
    val selectedTile = selected.tile()
    val opacity = Settings.LinkOpacity
    G.drawSurroundingCircle(this, x, y, R.C.Receiver, alpha = opacity)
    G.drawArrowLine(
        selected.block,
        selectedTile.x, selectedTile.y,
        this, x.toShort(), y.toShort(),
        ArrowDensity, R.C.Receiver, alpha = opacity, size = Settings.LinkSize
    )
}
@ClientOnly
inline fun whenNotConfiguringSender(func: () -> Unit) {
    if (!isConfiguringSender()) {
        func()
    }
}
@ClientOnly
fun IStreamHost.drawStreamGraphic(showCircle: Boolean = true) {
    if (clientConnectionNumber <= 0) return
    if (showCircle) {
        G.drawSurroundingCircle(tile, hostColor, alpha = Settings.LinkOpacity)
    }
    this.drawClients(connectedClients, showCircle)
}
@ClientOnly
fun IStreamClient.drawStreamGraphic(showCircle: Boolean = true) {
    if (hostConnectionNumber <= 0) return
    if (showCircle) {
        G.drawSurroundingCircle(tile, clientColor, alpha = Settings.LinkOpacity)
    }
    this.drawHosts(connectedHosts, showCircle)
}
@ClientOnly
fun IStreamClient.drawRequirements() {
    val reqs = this.requirements
    if (reqs != null) {
        G.drawMaterialIcons(this.building, reqs, Settings.LinkOpacity * 0.8f)
    }
}
/**
 * Called in an [IStreamClient] block
 *
 * @param x        tile x
 * @param y        tile y
 */
@ClientOnly
fun Block.drawLinkedLineToClientWhenConfiguring(x: Int, y: Int) {
    if (!Vars.control.input.config.isShown) return
    val selected = Vars.control.input.config.selected
    if (selected !is IStreamHost) {
        return
    }
    val host = selected as IStreamHost
    val selectedTile = selected.tile()
    val opacity = Settings.LinkOpacity
    G.drawSurroundingCircle(this, x, y, R.C.Client, alpha = opacity)
    G.drawArrowLine(
        selected.block,
        selectedTile.x, selectedTile.y,
        this, x.toShort(), y.toShort(),
        ArrowDensity, host.hostColor,
        alpha = opacity, size = Settings.LinkSize
    )
}
@ClientOnly
inline fun whenNotConfiguringHost(func: () -> Unit) {
    if (!isConfiguringHost()) {
        func()
    }
}
/**
 * Called in Receiver block
 */
@ClientOnly
fun IDataReceiver.drawSender(sender: Int?, showCircle: Boolean = true) {
    if (sender == null) {
        return
    }
    val sb = Vars.world.build(sender)
    val opacity = Settings.LinkOpacity
    if (sb is IDataSender) {
        val senderT = sb.tile()
        if (showCircle) {
            G.drawSurroundingCircle(senderT, sb.senderColor, alpha = opacity)
        }
        G.drawArrowLine(
            sb, this.building, ArrowDensity, this.receiverColor,
            alpha = opacity, size = Settings.LinkSize
        )
    }
}
/**
 * Called in Receiver block
 */
@ClientOnly
fun IDataReceiver.drawSenders(senders: Iterable<Int>, showCircle: Boolean = true) {
    val opacity = Settings.LinkOpacity
    for (sender in senders) {
        val sb = Vars.world.build(sender)
        if (sb is IDataSender) {
            val senderT = sb.tile()
            if (showCircle) {
                G.drawSurroundingCircle(senderT, sb.senderColor, alpha = opacity)
            }
            G.drawArrowLine(
                sb, this.building, ArrowDensity, this.receiverColor,
                alpha = opacity, size = Settings.LinkSize
            )
        }
    }
}
/**
 * Called in Sender block
 */
@ClientOnly
fun IDataSender.drawReceiver(receiver: Int?, showCircle: Boolean = true) {
    if (receiver == null) {
        return
    }
    val opacity = Settings.LinkOpacity
    val rb = Vars.world.build(receiver)
    if (rb is IDataReceiver) {
        val receiverT = rb.tile()
        if (showCircle) {
            G.drawSurroundingCircle(receiverT, rb.receiverColor, alpha = opacity)
        }
        G.drawArrowLine(
            this.building, rb, ArrowDensity, this.senderColor,
            alpha = opacity, size = Settings.LinkSize
        )
        rb.drawRequirements()
    }
}
/**
 * Called in Sender block
 */
@ClientOnly
fun IDataSender.drawReceivers(receivers: Iterable<Int>, showCircle: Boolean = true) {
    val original = Draw.z()
    val opacity = Settings.LinkOpacity
    for (receiver in receivers) {
        val rb = Vars.world.build(receiver)
        if (rb is IDataReceiver) {
            val receiverT = rb.tile()
            Draw.z(original)
            if (showCircle) {
                G.drawSurroundingCircle(receiverT, rb.receiverColor, alpha = opacity)
            }
            G.drawArrowLine(
                this.building, rb, ArrowDensity, this.senderColor,
                alpha = opacity, size = Settings.LinkSize
            )
            rb.drawRequirements()
        }
    }
    Draw.z(original)
}
@ClientOnly
fun isConfiguringSender(): Boolean {
    val selected = Vars.control.input.config.selected
    return selected is IDataSender
}
/**
 * Called in Client block
 */
@ClientOnly
fun IStreamClient.drawHosts(hosts: Iterable<Int>, showCircle: Boolean = true) {
    val opacity = Settings.LinkOpacity
    for (host in hosts) {
        val hostB = Vars.world.build(host)
        if (hostB is IStreamHost) {
            val hostT = hostB.tile()
            if (showCircle) {
                G.drawSurroundingCircle(hostT, hostB.hostColor, alpha = opacity)
            }
            G.drawArrowLine(
                hostB, this.building, ArrowDensity, this.clientColor,
                alpha = opacity, size = Settings.LinkSize
            )
        }
    }
}
/**
 * Called in Host block
 */
@ClientOnly
fun IStreamHost.drawClients(clients: Iterable<Int>, showCircle: Boolean = true) {
    val opacity = Settings.LinkOpacity
    for (client in clients) {
        val clientB = Vars.world.build(client)
        if (clientB is IStreamClient) {
            val clientT = clientB.tile()
            if (showCircle) {
                G.drawSurroundingCircle(clientT, clientB.clientColor, alpha = opacity)
            }
            G.drawArrowLine(
                this.building, clientB, ArrowDensity, this.hostColor,
                alpha = opacity, size = Settings.LinkSize
            )
            clientB.drawRequirements()
        }
    }
}
@ClientOnly
fun isConfiguringHost(): Boolean {
    val selected = Vars.control.input.config.selected
    return selected is IStreamHost
}
@ClientOnly
fun Building.drawOverRangeOn(other: Building) {
    Toaster.post(ToastTime) {
        Text.drawText {
            val tip = "${block.contentType}.${block.name}.over-range".bundle
            setText(it, tip)
            it.color.set(R.C.RedAlert).a(fadeInOutPct(ToastTimeFadePercent))
            it.draw(tip, other.x, other.y + 1f, Align.center)
        }
    }
}