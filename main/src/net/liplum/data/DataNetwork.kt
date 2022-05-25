package net.liplum.data

import arc.struct.IntSet
import mindustry.gen.Building
import mindustry.world.blocks.power.PowerGraph
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
    fun reflow(node: INetworkNode) {
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

    fun clear() {
        nodes.clear()
        entity.remove()
    }

    override fun toString() =
        "DataNetwork#$id"
}