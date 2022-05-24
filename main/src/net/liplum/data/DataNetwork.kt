package net.liplum.data

import arc.struct.IntSet
import net.liplum.api.cyber.INetworkNode
import java.util.*

class DataNetwork {
    val entity = DataNetworkUpdater.create()
    val nodes = ArrayList<INetworkNode>()
    private val tmp1 = ArrayList<INetworkNode>()
    private val tmp2 = ArrayList<INetworkNode>()
    private val bfsQueue = LinkedList<INetworkNode>()
    private val closedSet = IntSet()
    var id = lastNetworkID++
        private set

    companion object {
        @JvmStatic
        private var lastNetworkID = 0
    }

    fun update() {
    }

    fun addNetwork(other: DataNetwork) {
        if (other == this) return
        other.entity.remove()
        other.nodes.forEach(::add)
    }

    fun add(node: INetworkNode) {
        val dn = node.data
        if (dn.network != this || !dn.init) {
            dn.network = this
            dn.init = true
            nodes.add(node)
            entity.add()
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
            for (next in child.getNetworkConnections(tmp2)) {
                if (closedSet.add(next.building.pos())) {
                    bfsQueue.addLast(next)
                }
            }
        }
    }

    fun separate(node: INetworkNode) {
        val nodeConnection = node.getNetworkConnections(tmp1)
        if (nodeConnection.isEmpty()) {
            val newGraph = DataNetwork()
            newGraph.add(node)
            return
        }
        for (other in nodeConnection) {
            // Skip anyone isn't in the same graph
            if (other.data.network != this) continue
            // Create a new data network graph for this branch
            val newGraph = DataNetwork()
            newGraph.add(other)
            bfsQueue.clear()
            bfsQueue.addLast(node)
            while (bfsQueue.isNotEmpty()) {
                // Pop current one
                val child = bfsQueue.removeFirst()
                // Add it to the new branch
                newGraph.add(child)
                // Go through its connections
                for (next in child.getNetworkConnections(tmp2)) {
                    // Skip self
                    if (next != node && next.data.network != newGraph) {
                        newGraph.add(next)
                        bfsQueue.addLast(next)
                    }
                }
            }
        }
    }

    fun clear() {
        nodes.clear()
        entity.remove()
    }

    override fun toString() =
        "DataNetwork#$id"
}