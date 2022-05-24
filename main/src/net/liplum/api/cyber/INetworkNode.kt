package net.liplum.api.cyber

import mindustry.Vars
import net.liplum.api.ICyberEntity
import net.liplum.lib.Out

interface INetworkNode : ICyberEntity {
    var dataNetwork: NetworkModule
    fun getNetworkConnections(@Out out: MutableList<INetworkNode>): List<INetworkNode> {
        out.clear()
        val network: NetworkModule = dataNetwork
        val links = network.links
        for (i in 0 until links.size) {
            val link = Vars.world.tile(links[i])
            val b = link?.build
            if (b != null && b.team == building.team) {
                val node = b.getCyberEntity<INetworkNode>()
                if (node != null) {
                    out.add(node)
                }
            }
        }
        return out
    }
}