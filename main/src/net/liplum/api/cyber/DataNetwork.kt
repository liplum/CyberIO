package net.liplum.api.cyber

import arc.struct.IntSet
import arc.util.pooling.Pool
import arc.util.pooling.Pools
import mindustry.world.blocks.payloads.Payload
import plumy.pathkt.BFS
import plumy.pathkt.EasyBFS
import plumy.pathkt.LinkedPath
import java.util.*

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

    fun remove() {
        entity.remove()
        nodes.clear()
        routineCache.clear()
    }

    fun addNetwork(other: DataNetwork) {
        if (other == this) return
        other.entity.remove()
        other.nodes.forEach(::add)
    }

    fun add(node: INetworkNode) {
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
        var i = 0
        for (node in nodes) {
            val dataList = node.dataList.allData
            for (data in dataList) {
                func(i, node, data)
                i++
            }
        }
    }

    inline fun forEachData(func: (INetworkNode, Payload) -> Unit) {
        for (node in nodes) {
            val dataList = node.dataList.allData
            for (data in dataList)
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
            { Pools.obtain(Pointer::class.java, DataNetwork::Pointer) },
            { Pools.obtain(Path::class.java, DataNetwork::Path) },
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