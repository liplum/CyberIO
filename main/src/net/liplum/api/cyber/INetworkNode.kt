package net.liplum.api.cyber

import arc.math.geom.Rect
import arc.math.geom.Vec2
import mindustry.Vars
import mindustry.world.Block
import net.liplum.CLog
import net.liplum.Var
import net.liplum.api.ICyberEntity
import net.liplum.api.cyber.SideLinks.Companion.coordinates
import net.liplum.api.cyber.SideLinks.Companion.reflect
import net.liplum.common.util.isEven
import net.liplum.common.util.raycastInThis
import net.liplum.data.DataID
import net.liplum.data.EmptyDataID
import net.liplum.data.PayloadData
import net.liplum.data.PayloadDataList
import plumy.core.ClientOnly
import plumy.core.Out
import plumy.core.Serialized
import plumy.core.math.Progress
import plumy.pathkt.IVertex
import plumy.dsl.TileXY
import plumy.dsl.WorldXY
import plumy.dsl.castBuild

interface INetworkNode : ICyberEntity, IVertex<INetworkNode> {
    var network: DataNetwork
    var init: Boolean
    var links: SideLinks
    @Serialized
    var request: DataID
    @Serialized
    var dataInSending: DataID
    @Serialized
    val dataList: PayloadDataList
    /**
     * It represents which next node this wants to send.
     */
    @Serialized
    var currentOriented: Side
    val currentOrientedNode: INetworkNode?
        get() = if (currentOriented == -1) null
        else links[currentOriented].castBuild()
    var sendingProgress: Progress
        get() = if (totalSendingDistance == 0f) 0f else curSendingLength / totalSendingDistance
        set(value) {
            curSendingLength = totalSendingDistance * value
        }
    val sideEnable: SideEnable
    val canReceiveMoreData: Boolean
        get() = dataList.canAddMore
    /**
     * 4 sides
     */
    @ClientOnly
    val linkingTime: FloatArray
    var livingTime: Float
    /**
     * 4 sides
     */
    @ClientOnly
    val lastRailTrail: Array<RailTrail>
    @Serialized
    var totalSendingDistance: Float
    @Serialized
    var curSendingLength: Float
    fun updateAsNode() {
        val delta = deltaAsNode()
        livingTime += delta
        links.forEach {
            if (links.isEmpty(it)) linkingTime[it] = 0f
            else linkingTime[it] += delta
        }
        if (currentOriented != -1) {
            getData(dataInSending)?.let {
                curSendingLength = (curSendingLength + getDataSendingDstDelta(it)).coerceIn(0f, totalSendingDistance)
                val nextNode = currentOrientedNode
                if (nextNode != null) {
                    if (sendingProgress >= 1f && trySendDataTo(nextNode, it)) {
                        sendingProgress = 0f
                        advanceProgress()
                    }
                }
            }
        }
    }

    fun trySendDataTo(next: INetworkNode, data: PayloadData): Boolean {
        if (next.canReceiveMoreData) {
            next.addData(data)
            dataList.remove(data)
            return true
        }
        return false
    }
    /**
     * Reset [currentOriented] and [dataInSending]
     */
    fun advanceProgress() {
        network.advanceProgress(dataInSending)
        dataInSending = EmptyDataID
        currentOriented = -1
    }
    /**
     * Each [WorldXY] unit size requires 10 ticks to be sent as default.
     */
    fun getDataSendingDstDelta(payload: PayloadData): Progress {
        return deltaAsNode() * Var.NetworkNodeSendingSpeed
    }

    fun deltaAsNode(): Float =
        building.delta()
    /**
     * If this couldn't contain more, just desert it.
     * Note: even if unsync, the [dataList] will correct the result.
     */
    fun addData(payload: PayloadData) {
        if (dataList.canAddMore) {
            dataList.add(payload)
            if (payload.id == request)
                request = EmptyDataID
            network.onDataInventoryChanged()
        }
    }

    fun receiveData(payload: PayloadData) {
        addData(payload)
    }

    fun setOriented(next: INetworkNode): Boolean {
        val nextPos = next.building.pos()
        links.forEachPosWithSide { side, pos ->
            if (pos == nextPos) {
                if (currentOriented != side) {
                    currentOriented = side
                    totalSendingDistance = this.calcuDistanceTo(next)
                    curSendingLength = 0f
                }
                return true
            }
        }
        return false
    }

    fun getSide(next: INetworkNode): Side {
        val nextPos = next.building.pos()
        links.forEachPosWithSide { side, pos ->
            if (pos == nextPos) return side
        }
        return -1
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
        clearStatus()
    }

    fun clearStatus() {
        livingTime = 0f
        curSendingLength = 0f
        totalSendingDistance = 0f
        ClientOnly {
            lastRailTrail.forEach {
                it.curLength = 0f
                it.totalLength = 0f
            }
            for (i in linkingTime.indices) {
                linkingTime[i] = 0f
            }
        }
    }
    /**
     * Check whether the [other] is in this link bound
     * @return true if this can link to it.
     * Note: It doesn't guarantee [other] can link this.
     */
    fun isInLinkBound(side: Side, other: INetworkNode): Boolean {
        this.building.hitbox(rect)
        return rect.raycastInThis(
            tempVec.set(other.building),
            tempVec2.set(tempVec).add(
                coordinates[side].x * this.linkRange,
                coordinates[side].y * this.linkRange
            ),
        ) != null
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
        // This is necessary, otherwise, you will see two nodes linking to a node on one side.
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
        lastRailTrail[side].let {
            it.curLength = 0f
            it.totalLength = 0f
        }
        old.castBuild<INetworkNode>()?.let {
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
        private val tempVec = Vec2()
        private val tempVec2 = Vec2()
        private val rect = Rect()
    }
}

class RailTrail(
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
        if (block.size.isEven) {
            CLog.warn("[$this]It only allow an odd size of a NetworkNode block but ${block.size} is given, and now it's revised to ${block.size + 1}.")
            block.size += 1
        }
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
    override var dataInSending: DataID = EmptyDataID
    override val dataList: PayloadDataList = PayloadDataList()
    @ClientOnly
    override val expendSelectingLineTime = 0f
    override var currentOriented: Side = -1
    override var sendingProgress: Float = 0f
    override val sideEnable = SideEnable(4) { false }
    override val linkingTime = FloatArray(4)
    override var livingTime = 0f
    override val lastRailTrail = Array(4) { RailTrail() }
    override var totalSendingDistance = 0f
    override var curSendingLength = 0f
    override val linkRange = 0f
}

fun INetworkNode.inTheSameNetwork(other: INetworkNode): Boolean =
    this.network == other.network
