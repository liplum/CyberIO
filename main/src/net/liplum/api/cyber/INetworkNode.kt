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
    val dataList: PayloadDataList
    /**
     * It represents which next node this wants to send.
     */
    @Serialized
    var currentOriented: Side
    /**
     * [0f,1f]
     */
    @Serialized
    val sendingProgress: Float
    val sideEnable: SideEnable
    val canReceiveMoreData: Boolean
        get() = dataList.canAddMore
    val railWidth: Float
        get() = Vars.tilesize.toFloat()
    /**
     * 4 sides
     * [0f,1f]
     */
    @ClientOnly
    val warmUp: FloatArray
    fun updateAsNode() {
        links.forEachSide {
            if (links.isEmpty(it)) warmUp[it] = 0f
            else warmUp[it] = (warmUp[it] + deltaAsNode() * 0.008f
                    ).coerceIn(0f, 1f)
        }
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
        warmUp[side] = 0f
        old.TEAny<INetworkNode>()?.let {
            // If this is a node existed on this side, separate the network
            it.links[side.reflect] = -1
            it.warmUp[side.reflect] = 0f
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
    val linkRange: WorldXY
    val tileLinkRange: TileXY
        get() = (linkRange / Vars.tilesize).toInt()
    val block: Block
    val dataCapacity: Int
    @ClientOnly
    val expendPlacingLineTime: Float
    val sideEnable: SideEnable
    fun setupNetworkNodeSettings() {
        block.apply {
            update = true
            configurable = true
            sync = true
        }
    }
}

fun INetworkNode.hasData(id: DataID): Boolean =
    dataList.hasData(id)
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
    override val dataList: PayloadDataList = PayloadDataList()
    @ClientOnly
    override val expendSelectingLineTime = 0f
    override var currentOriented: Side = -1
    override val sendingProgress: Float = 0f
    override val sideEnable = SideEnable(4) { false }
    override val warmUp = FloatArray(4)
    override val linkRange = 0f
}

fun INetworkNode.inTheSameNetwork(other: INetworkNode): Boolean =
    this.network == other.network
