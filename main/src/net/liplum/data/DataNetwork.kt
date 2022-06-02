package net.liplum.data

import arc.struct.IntSet
import arc.util.pooling.Pool
import arc.util.pooling.Pools
import mindustry.world.blocks.payloads.Payload
import net.liplum.api.cyber.INetworkNode
import net.liplum.api.cyber.SideLinks
import net.liplum.mdt.utils.NewEmptyPos
import net.liplum.mdt.utils.Pos
import plumy.pathkt.BFS
import plumy.pathkt.EasyBFS
import plumy.pathkt.LinkedPath
import java.util.*

object EmptyNetworkNode : INetworkNode {
    override var network = DataNetwork()
    override var init = true
    override var links = SideLinks()
    override val data = PayloadData()
    override val currentOriented: Pos = NewEmptyPos()
    override val sendingProgress: Float = 0f
    override var routine: DataNetwork.Path? = DataNetwork.Path()
    override val linkRange = 0f
    override val maxLink = 0
}

class DataNetwork {
    val entity = DataNetworkUpdater.create()
    val nodes = ArrayList<INetworkNode>()
    val routineCache = HashMap<Any, Path>()
    var id = lastNetworkID++
        private set

    fun onNetworkNodeChanged() {
        routineCache.clear()
    }

    fun update() {
    }

    fun addNetwork(other: DataNetwork) {
        if (other == this) return
        other.entity.remove()
        other.nodes.forEach(::add)
    }

    private fun add(node: INetworkNode) {
        if (node.network != this || !node.init) {
            node.network = this
            node.init = true
            nodes.add(node)
            entity.add()
            onNetworkNodeChanged()
        }
    }

    fun merge(node: INetworkNode) {
        if (node.network == this) return
        node.network.entity.remove()
        // iterate its link
        entity.add()
        bfsQueue.clear()
        bfsQueue.addLast(node)
        closedSet.clear()
        while (bfsQueue.size > 0) {
            val child = bfsQueue.removeFirst()
            add(child)
            for (next in child.linkedVertices) {
                if (closedSet.add(next.building.pos())) {
                    bfsQueue.addLast(next)
                }
            }
        }
        onNetworkNodeChanged()
    }

    fun reflow(node: INetworkNode) {
        bfsQueue.clear()
        bfsQueue.addLast(node)
        closedSet.clear()
        while (bfsQueue.size > 0) {
            val child = bfsQueue.removeFirst()
            add(child)
            for (next in child.linkedVertices) {
                if (closedSet.add(next.building.pos())) {
                    bfsQueue.addLast(next)
                }
            }
        }
        onNetworkNodeChanged()
    }

    inline fun forEachDataIndexed(func: (Int, INetworkNode, Payload) -> Unit) {
        for ((i, node) in nodes.withIndex()) {
            val data = node.data.data
            if (data != null)
                func(i, node, data)
        }
    }

    inline fun forEachData(func: (INetworkNode, Payload) -> Unit) {
        for (node in nodes) {
            val data = node.data.data
            if (data != null)
                func(node, data)
        }
    }

    class Path : LinkedPath<INetworkNode>(), Pool.Poolable {
        override fun reset() {
            path.clear()
        }
    }

    class Pointer : BFS.IPointer<INetworkNode>, Pool.Poolable {
        override var previous: BFS.IPointer<INetworkNode>? = null
        override var self: INetworkNode = EmptyNetworkNode
        override fun reset() {
            previous = null
            self = EmptyNetworkNode
        }
    }

    override fun toString() =
        "DataNetwork#$id"

    companion object {
        private val bfs = EasyBFS<INetworkNode, Path>(
            { Pools.obtain(Pointer::class.java, ::Pointer) },
            { Pools.obtain(Path::class.java, ::Path) },
        )
        private val tmp1 = ArrayList<INetworkNode>()
        private val bfsQueue = LinkedList<INetworkNode>()
        private val closedSet = IntSet()
        @JvmStatic
        private var lastNetworkID = 0
        fun genStart2DestinationKey(start: INetworkNode, dest: INetworkNode): Any =
            start.building.id * 31 + dest.building.id
    }
}