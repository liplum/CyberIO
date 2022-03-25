@file:JvmName("CyberH")

package net.liplum.api.cyber

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.util.Tmp
import mindustry.Vars
import mindustry.type.Item
import mindustry.type.Liquid
import mindustry.world.Block
import net.liplum.R
import net.liplum.ui.Settings
import net.liplum.utils.*

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

val IDataBuilding?.exists: Boolean
    get() = this != null && this.building.exists

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
    fun loadLiquidsColor() {
        val liquids = Vars.content.liquids()
        R.C.LiquidColors = Array(liquids.size) {
            liquids[it].color
        }
    }
    @JvmStatic
    fun initStreamColors() {
        R.C.HostLiquidColors = Array(R.C.LiquidColors.size) {
            R.C.LiquidColors[it].cpy().lerp(R.C.Host, 0.4f)
        }
        R.C.ClientLiquidColors = Array(R.C.LiquidColors.size) {
            R.C.LiquidColors[it].cpy().lerp(R.C.Client, 0.4f)
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

val Liquid?.hostColor: Color
    get() = if (this == null)
        R.C.Host
    else
        R.C.HostLiquidColors[this.ID]
val Liquid?.clientColor: Color
    get() = if (this == null)
        R.C.Client
    else
        R.C.ClientLiquidColors[this.ID]

fun Float.isAccepted() =
    this <= -1f || this > 0f

fun IDataSender.drawDataNetGraphic() {
    G.drawSurroundingCircle(tile, R.C.Sender)
    if (canMultipleConnect()) {
        this.drawReceivers(connectedReceivers())
    } else {
        this.drawReceiver(connectedReceiver())
    }
}

fun IDataReceiver.drawDataNetGraphic() {
    G.drawSurroundingCircle(tile, R.C.Receiver)
    this.drawSenders(connectedSenders())
}

fun IDataReceiver.drawRequirements() {
    val reqs = this.requirements
    if (reqs != null) {
        G.drawMaterialIcons(this.building, reqs)
    }
}
/**
 * Called in an [IDataReceiver] block
 *
 * @param x        tile x
 * @param y        tile y
 */
fun Block.drawLinkedLineToReceiverWhenConfiguring(x: Int, y: Int) {
    if (!Vars.control.input.frag.config.isShown) return
    val selected = Vars.control.input.frag.config.selectedTile
    if (selected !is IDataSender) {
        return
    }
    val selectedTile = selected.tile()
    G.drawSurroundingCircle(this, x, y, R.C.Receiver)
    G.drawArrowLine(
        selected.block,
        selectedTile.x, selectedTile.y,
        this, x.toShort(), y.toShort(),
        ArrowDensity, R.C.Receiver
    )
}

inline fun whenNotConfiguringSender(func: () -> Unit) {
    if (!isConfiguringSender()) {
        func()
    }
}

fun IStreamHost.drawStreamGraphic() {
    G.drawSurroundingCircle(tile, hostColor)
    this.drawClients(connectedClients())
}

fun IStreamClient.drawStreamGraphic() {
    G.drawSurroundingCircle(tile, clientColor)
    this.drawHosts(connectedHosts())
}

fun IStreamClient.drawRequirements() {
    val reqs = this.requirements
    if (reqs != null) {
        G.drawMaterialIcons(this.building, reqs)
    }
}
/**
 * Called in an [IStreamClient] block
 *
 * @param x        tile x
 * @param y        tile y
 */
fun Block.drawLinkedLineToClientWhenConfiguring(x: Int, y: Int) {
    if (!Vars.control.input.frag.config.isShown) return
    val selected = Vars.control.input.frag.config.selectedTile
    if (selected !is IStreamHost) {
        return
    }
    val host = selected as IStreamHost
    val selectedTile = selected.tile()
    G.drawSurroundingCircle(this, x, y, R.C.Client)
    G.drawArrowLine(
        selected.block,
        selectedTile.x, selectedTile.y,
        this, x.toShort(), y.toShort(),
        ArrowDensity, host.hostColor
    )
}

inline fun whenNotConfiguringHost(func: () -> Unit) {
    if (!isConfiguringHost()) {
        func()
    }
}

var ArrowDensity = 15f
/**
 * Called in Receiver block
 */
fun IDataReceiver.drawSender(sender: Int?) {
    if (sender == null) {
        return
    }
    val sb = Vars.world.build(sender)
    if (sb is IDataSender) {
        val senderT = sb.tile()
        G.drawSurroundingCircle(senderT, R.C.Sender)
        G.drawArrowLine(sb, this.building, ArrowDensity, R.C.Receiver)
    }
    /* TODO: For payload
    else {
        if (sb is PayloadConveyor.PayloadConveyorBuild) {
            if ((sb.payload as? BuildPayload)?.build is IDataSender) {
                G.drawSurroundingCircle(sb.tile, R.C.Sender)
                G.drawArrowLine(sb, this.building, ArrowDensity, R.C.Receiver)
            }
        }
    }*/
}
/**
 * Called in Receiver block
 */
fun IDataReceiver.drawSenders(senders: Iterable<Int>) {
    val opacity = Settings.LinkOpacity
    for (sender in senders) {
        val sb = Vars.world.build(sender)
        if (sb is IDataSender) {
            val senderT = sb.tile()
            G.drawSurroundingCircle(senderT, R.C.Sender, alpha = opacity)
            G.drawArrowLine(sb, this.building, ArrowDensity, R.C.Receiver, alpha = opacity)
        }
    }
}
/**
 * Called in Sender block
 */
fun IDataSender.drawReceiver(receiver: Int?) {
    if (receiver == null) {
        return
    }
    val opacity = Settings.LinkOpacity
    val rb = Vars.world.build(receiver)
    if (rb is IDataReceiver) {
        val receiverT = rb.tile()
        G.drawSurroundingCircle(receiverT, R.C.Receiver, alpha = opacity)
        G.drawArrowLine(this.building, rb, ArrowDensity, R.C.Sender, alpha = opacity)
        rb.drawRequirements()
    }
    /* TODO: For payload
    else if (rb is PayloadConveyor.PayloadConveyorBuild) {
        val dr = (rb.payload as? BuildPayload)?.build as? IDataReceiver
        if (dr != null) {
            G.drawSurroundingCircle(rb.tile, R.C.Receiver)
            G.drawArrowLine(this.building, rb, ArrowDensity, R.C.Sender)
            dr.drawRequirements()
        }
    }*/
}
/**
 * Called in Sender block
 */
fun IDataSender.drawReceivers(receivers: Iterable<Int>) {
    val original = Draw.z()
    val opacity = Settings.LinkOpacity
    for (receiver in receivers) {
        val rb = Vars.world.build(receiver)
        if (rb is IDataReceiver) {
            val receiverT = rb.tile()
            Draw.z(original)
            G.drawSurroundingCircle(receiverT, R.C.Receiver, alpha = opacity)
            G.drawArrowLine(this.building, rb, ArrowDensity, R.C.Sender, alpha = opacity)
            rb.drawRequirements()
        }
    }
    Draw.z(original)
}

fun isConfiguringSender(): Boolean {
    val selected = Vars.control.input.frag.config.selectedTile
    return selected is IDataSender
}
/**
 * Called in Client block
 */
fun IStreamClient.drawHosts(hosts: Iterable<Int>) {
    for (host in hosts) {
        val hostB = Vars.world.build(host)
        if (hostB is IStreamHost) {
            val hostT = hostB.tile()
            G.drawSurroundingCircle(hostT, hostB.hostColor)
            G.drawArrowLine(hostB, this.building, ArrowDensity, this.clientColor)
        }
    }
}
/**
 * Called in Host block
 */
fun IStreamHost.drawClients(clients: Iterable<Int>) {
    for (client in clients) {
        val clientB = Vars.world.build(client)
        if (clientB is IStreamClient) {
            val clientT = clientB.tile()
            G.drawSurroundingCircle(clientT, clientB.clientColor)
            G.drawArrowLine(this.building, clientB, ArrowDensity, this.hostColor)
            clientB.drawRequirements()
        }
    }
}

fun isConfiguringHost(): Boolean {
    val selected = Vars.control.input.frag.config.selectedTile
    return selected is IStreamHost
}
