package net.liplum.data

import arc.struct.IntSet
import arc.util.pooling.Pool
import arc.util.pooling.Pools
import mindustry.world.blocks.payloads.Payload
import net.liplum.api.cyber.INetworkNode
import net.liplum.api.cyber.NetworkModule
import net.liplum.lib.delegates.Delegate
import net.liplum.mdt.utils.NewEmptyPos
import net.liplum.mdt.utils.Pos
import plumy.pathkt.BFS
import plumy.pathkt.EasyBFS
import plumy.pathkt.LinkedPath
import java.util.*

class EmptyNetworkNode : INetworkNode {
    override var dataMod = NetworkModule()
    override val data = PayloadData()
    override val currentOriented: Pos = NewEmptyPos()
    override val sendingProgress: Float = 0f
    override var routine: DataNetwork.Path? = DataNetwork.Path()
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

    fun add(node: INetworkNode) {
        val dn = node.dataMod
        if (dn.network != this || !dn.init) {
            dn.network = this
            dn.init = true
            nodes.add(node)
            entity.add()
            onNetworkNodeChanged()
        }
    }

    fun merge(node: INetworkNode) {
        if (node.networkGraph == this) return
        node.networkGraph.entity.remove()
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
        override var self: INetworkNode = emptyNode
        override fun reset() {
            previous = null
            self = emptyNode
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
        private val emptyNode = EmptyNetworkNode()
        fun genStart2DestinationKey(start: INetworkNode, dest: INetworkNode): Any =
            start.building.id * 31 + dest.building.id
    }
}