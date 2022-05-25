package net.liplum.api.cyber

import mindustry.Vars
import net.liplum.api.ICyberEntity
import net.liplum.data.DataNetwork
import net.liplum.lib.Out

interface INetworkNode : ICyberEntity {
    var data: NetworkModule
    var networkGraph: DataNetwork
        get() = data.network
        set(value) {
            data.network = value
        }

    fun link(target: INetworkNode) {
        data.links.addUnique(target.building.pos())
        target.data.links.addUnique(this.building.pos())
        this.data.network.merge(target)
    }

    fun unlink(target: INetworkNode) {
        data.links.removeValue(target.building.pos())
        target.data.links.removeValue(this.building.pos())
        val selfNewGraph = DataNetwork()
        selfNewGraph.reflow(this)
        val targetNewGraph = DataNetwork()
        targetNewGraph.reflow(target)
    }

    fun getNetworkConnections(@Out out: MutableList<INetworkNode>): MutableList<INetworkNode> {
        out.clear()
        val network: NetworkModule = data
        val links = network.links
        for (i in 0 until links.size) {
            val link = Vars.world.tile(links[i])
            val b = link?.build
            if (b != null && b.team == building.team) {
                val node = b.nn()
                if (node != null) {
                    out.add(node)
                }
            }
        }
        return out
    }
}