package net.liplum.api.cyber

import mindustry.Vars
import mindustry.world.Block
import net.liplum.api.ICyberEntity
import net.liplum.api.cyber.SideLinks.Companion.reflect
import net.liplum.data.DataID
import net.liplum.data.EmptyDataID
import net.liplum.data.PayloadData
import net.liplum.data.PayloadDataList
import net.liplum.lib.Out
import net.liplum.lib.Serialized
import net.liplum.lib.math.Progress
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.TEAny
import net.liplum.mdt.utils.TileXY
import net.liplum.mdt.utils.WorldXY
import plumy.pathkt.IVertex

interface INetworkNode : ICyberEntity, IVertex<INetworkNode> {
    var network: DataNetwork
    var init: Boolean
    var links: SideLinks
    @Serialized
    var request: DataID
    @Serialized
    var dataBeingSent: DataID
    @Serialized
    val dataList: PayloadDataList
    /**
     * It represents which next node this wants to send.
     */
    @Serialized
    var currentOriented: Side
    val currentOrientedNode: INetworkNode?
        get() = if (currentOriented == -1) null
        else links[currentOriented].TEAny()
    /**
     * [0f,1f]
     */
    @Serialized
    var sendingProgress: Progress
    val sideEnable: SideEnable
    val canReceiveMoreData: Boolean
        get() = dataList.canAddMore
    /**
     * 4 sides
     */
    @ClientOnly
    val linkingTime: FloatArray
    /**
     * 4 sides
     */
    @ClientOnly
    val lastRailEntry: Array<RailEntry>
    fun updateAsNode() {
        links.forEach {
            if (links.isEmpty(it)) linkingTime[it] = 0f
            else linkingTime[it] += deltaAsNode()
        }
        getData(dataBeingSent)?.let {
            sendingProgress += getDataSendingIncrement(it)
            sendingProgress = sendingProgress.coerceIn(0f, 1f)
            if (sendingProgress >= 1f) {
                val next = currentOrientedNode
                if (next != null && trySendDataTo(next, it)) {
                    sendingProgress = 0f
                    advanceProgress()
                }
            }
        }
    }

    fun trySendDataTo(next: INetworkNode, data: PayloadData): Boolean {
        if (next.canReceiveMoreData) {
            next.receiveData(data)
            dataList.remove(data)
            return true
        }
        return false
    }

    fun advanceProgress() {
        network.advanceProgress(dataBeingSent)
        dataBeingSent = EmptyDataID
        currentOriented = -1
    }
    /**
     * Each [WorldXY] unit size requires 10 ticks to be sent as default.
     */
    fun getDataSendingIncrement(payload: PayloadData): Progress {
        val totalTime = payload.payload.size() * 3f
        return deltaAsNode() / totalTime
    }

    fun deltaAsNode(): Float =
        building.delta()
    /**
     * If this couldn't contain more, just desert it.
     * Note: even if unsync, the [dataList] will correct the result.
     */
    fun receiveData(payload: PayloadData) {
        if (dataList.canAddMore) {
            dataList.add(payload)
            if(payload.id == request)
                request = EmptyDataID
        }
    }

    fun setOriented(next: INetworkNode): Boolean {
        val nextPos = next.building.pos()
        links.forEachPosWithSide { side, pos ->
            if (pos == nextPos) {
                currentOriented = side
                return true
            }
        }
        return false
    }
    /**
     * @param payload it doesn't check whether the [payload] is in this node.
     * The caller should guarantee that.
     * @return whether the [node] received the [payload]
     */
    fun sendDataToNextNode(payload: PayloadData, node: INetworkNode): Boolean {
        if (node.canReceiveMoreData) {
            node.receiveData(payload)
            dataList.remove(payload)
            return true
        }
        return false
    }

    override val linkedVertices: Iterable<INetworkNode>
        get() = getNetworkConnections(tempList)

    fun isLinkedWith(side: Side, other: INetworkNode) =
        other.building.pos() == links[side]

    fun isLinkedWith(other: INetworkNode) =
        other.building.pos() in links
    /**
     * @return [out]
     */
    fun getNetworkConnections(@Out out: MutableList<INetworkNode>):
            MutableList<INetworkNode> {
        out.clear()
        links.forEachNode { out.add(it) }
        return out
    }
    /**
     * Call this function when self is removed from the ground.
     * It will correct the network
     */
    fun onRemovedFromGround() {
        links.forEachSide { side ->
            // If this is a node existed on this side, separate the network
            clearSide(side)
        }
    }

    fun isSideFull(side: Side) =
        links[side] != -1
    @ClientOnly
    val expendSelectingLineTime: Float
    val linkRange: WorldXY
    val tileLinkRange: TileXY
        get() = (linkRange / Vars.tilesize).toInt()
    /**
     * It will merge [target] into self's network.
     * It will clear the old sides of each other
     * @param side relative to self's side
     */
    fun link(side: Side, target: INetworkNode) {
        // clear the old one
        clearSide(side)
        target.clearSide(side.reflect)

        this.links[side] = target
        target.links[side.reflect] = this
        DataNetwork.mergeToLagerNetwork(this, target)
    }
    /**
     * It takes the side into account.
     * @param side relative to self's side
     */
    fun canLink(side: Side, target: INetworkNode): Boolean {
        if (target.building.dst(building) > linkRange) return false
        val thisOld = this.links[side]
        val targetOld = target.links[side.reflect]
        if (thisOld != -1 && targetOld != -1) return false
        return true
    }
    /**
     * Clear this side and reflow the network.
     * If the side already held a node, reset its [INetworkNode.links] and create a new network for it.
     * it doesn't consider the enabled side
     */
    fun clearSide(side: Side) {
        val old = links[side]
        if (old == -1) return
        links[side] = -1
        old.TEAny<INetworkNode>()?.let {
            // If this is a node existed on this side, separate the network
            it.links[side.reflect] = -1
            if (it.network == this.network) {
                val newNetwork = DataNetwork()
                val oldNetwork = it.network
                // MUSTN'T: it.network = newNetwork
                oldNetwork.remove()
                newNetwork.reflow(it)
            }
        }
        this.network.reflow(this)
    }

    companion object {
        private val tempList = ArrayList<INetworkNode>()
    }
}

class RailEntry(
    /** Center X or Y  */
    var center: Float = 0f,
    /** current length  */
    var curLength: Float = 0f,
    var totalLength: Float = 0f,
)
/**
 * Only iterate the enabled sides
 */
inline fun INetworkNode.forEachEnabledSide(func: (Side) -> Unit) {
    for (side in RIGHT..BOTTOM) {
        if (sideEnable[side])
            func(side)
    }
}

interface INetworkBlock {
    var linkRange: WorldXY
    val tileLinkRange: TileXY
        get() = (linkRange / Vars.tilesize).toInt()
    val block: Block
    val dataCapacity: Int
    @ClientOnly
    val expendPlacingLineTime: Float
    val sideEnable: SideEnable
    /**
     * Call this in [Block]'s constructor.
     */
    fun setupNetworkNodeSettings() {
        block.apply {
            update = true
            configurable = true
            sync = true
        }
    }
    /**
     * Call this in [Block.init] before super one.
     */
    fun initNetworkNodeSettings() {
        linkRange += block.size / 2f
    }
}

fun INetworkNode.hasData(id: DataID): Boolean =
    dataList.hasData(id)

fun INetworkNode.getData(id: DataID): PayloadData? =
    dataList.getData(id)
/**
 * Only iterate the enabled sides
 */
inline fun INetworkBlock.forEachEnabledSide(func: (Side) -> Unit) {
    for (side in RIGHT..BOTTOM) {
        if (sideEnable[side])
            func(side)
    }
}

object EmptyNetworkNode : INetworkNode {
    override var network = DataNetwork()
    override var init = true
    override var links = SideLinks()
    override var request: DataID = EmptyDataID
    override var dataBeingSent: DataID = EmptyDataID
    override val dataList: PayloadDataList = PayloadDataList()
    @ClientOnly
    override val expendSelectingLineTime = 0f
    override var currentOriented: Side = -1
    override var sendingProgress: Float = 0f
    override val sideEnable = SideEnable(4) { false }
    override val linkingTime = FloatArray(4)
    override val lastRailEntry = Array(4) { RailEntry() }
    override val linkRange = 0f
}

fun INetworkNode.inTheSameNetwork(other: INetworkNode): Boolean =
    this.network == other.network
