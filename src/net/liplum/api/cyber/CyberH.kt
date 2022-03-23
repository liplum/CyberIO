package net.liplum.api.cyber

import arc.graphics.Color
import mindustry.Vars
import mindustry.type.Item
import mindustry.type.Liquid
import mindustry.world.Block
import net.liplum.R
import net.liplum.api.CyberU
import net.liplum.utils.G
import net.liplum.utils.ID
import net.liplum.utils.build
import net.liplum.utils.exists

fun Int.db(): IDataBuilding? =
    this.build as? IDataBuilding

fun Int.dr(): IDataReceiver? =
    this.build as? IDataReceiver

fun Int.ds(): IDataSender? =
    this.build as? IDataSender

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
        CyberU.drawReceivers(this, connectedReceivers())
    } else {
        CyberU.drawReceiver(this, connectedReceiver())
    }
}

fun IDataReceiver.drawDataNetGraphic() {
    G.drawSurroundingCircle(tile, R.C.Receiver)
    CyberU.drawSenders(this, connectedSenders())
}

fun IDataReceiver.drawRequirements() {
    CyberU.drawRequirements(this)
}

fun Block.drawLinkedLineToReceiverWhenConfiguring(x: Int, y: Int) {
    CyberU.drawLinkedLineToReceiverWhenConfiguring(this, x, y)
}

inline fun whenNotConfiguringSender(func: () -> Unit) {
    if (!CyberU.isConfiguringSender()) {
        func()
    }
}

fun IStreamHost.drawStreamGraphic() {
    G.drawSurroundingCircle(tile, hostColor)
    CyberU.drawClients(this, connectedClients())
}

fun IStreamClient.drawStreamGraphic() {
    G.drawSurroundingCircle(tile, clientColor)
    CyberU.drawHosts(this, connectedHosts())
}

fun IStreamClient.drawRequirements() {
    CyberU.drawRequirements(this)
}

fun Block.drawLinkedLineToClientWhenConfiguring(x: Int, y: Int) {
    CyberU.drawLinkedLineToClientWhenConfiguring(this, x, y)
}

inline fun whenNotConfiguringHost(func: () -> Unit) {
    if (!CyberU.isConfiguringHost()) {
        func()
    }
}
