package net.liplum.data

import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.meta.Env
import net.liplum.api.cyber.INetworkNode
import net.liplum.api.cyber.NetworkModule
import net.liplum.api.cyber.getCyberEntity
import net.liplum.mdt.CalledBySync
import net.liplum.mdt.render.G
import net.liplum.mdt.utils.PackedPos
import net.liplum.mdt.utils.build

class DataCDN(name: String) : Block(name) {
    init {
        update = true
        solid = true
        configurable = true
        envEnabled = envEnabled or Env.space
        config(Integer::class.java) { b: CdnBuild, i ->
            b.linkNodeFromRemote(i.toInt())
        }
        configClear { b: CdnBuild ->
            b.clearLinks()
        }
    }

    inner class CdnBuild : Building(), INetworkNode {
        override var dataNetwork = NetworkModule()
        @CalledBySync
        fun linkNodeFromRemote(pos: PackedPos) {
            dataNetwork.links.add(pos)
        }

        override fun draw() {
            super.draw()
            for (link in dataNetwork.links.items) {
                val linkB = link.build
                if (linkB != null)
                    G.drawDashLineBetweenTwoBlocksBreath(this.tile, linkB.tile)
            }
        }

        fun clearLinks() {
            dataNetwork.links.clear()
        }

        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            val node = other.getCyberEntity<INetworkNode>()
            if (node != null) {
                configure(other.pos())
                return false
            }
            return true
        }
    }
}