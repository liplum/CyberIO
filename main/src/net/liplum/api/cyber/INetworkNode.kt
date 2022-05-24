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

    fun getNetworkConnections(@Out out: MutableList<INetworkNode>): List<INetworkNode> {
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