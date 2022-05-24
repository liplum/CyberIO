package net.liplum.data

import net.liplum.api.cyber.INetworkNode
import java.util.*

class DataNetwork {
    val entity = DataNetworkUpdater.create()
    val nodes = ArrayList<INetworkNode>()
    private val tmp1 = ArrayList<INetworkNode>()
    private val tmp2 = ArrayList<INetworkNode>()
    val bfsQueue = LinkedList<INetworkNode>()
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
        val dn = node.dataNetwork
        if (dn.graph != null && dn.graph != this) {
            dn.graph.entity.remove()
            dn.graph = this
            nodes.add(node)
            entity.add()
        }
    }

    fun separate(node: INetworkNode) {
        for (other in node.getNetworkConnections(tmp1)) {
            // Skip anyone isn't in the same graph
            if (other.dataNetwork.graph != this) continue
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
                    if (next != node && next.dataNetwork.graph != newGraph) {
                        newGraph.add(next)
                        bfsQueue.addLast(next)
                    }
                }
            }
        }
        // Now this graph has been separated, it's empty now.
        entity.remove()
    }

    fun clear() {
        nodes.clear()
        entity.remove()
    }
}