package net.liplum.api.cyber

import mindustry.Vars
import net.liplum.api.ICyberEntity
import net.liplum.data.DataNetwork
import net.liplum.lib.Out
import net.liplum.lib.utils.forEach

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
        data.links.forEach {
            val link = Vars.world.tile(it)
            val b = link?.build
            if (b != null && b.team == building.team) {
                val node = b.nn()
                if (node != null)
                    out.add(node)
            }
        }
        return out
    }
}