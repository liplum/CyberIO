package net.liplum.data

import arc.graphics.Color
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.meta.Env
import net.liplum.DebugOnly
import net.liplum.api.cyber.INetworkNode
import net.liplum.api.cyber.NetworkModule
import net.liplum.api.cyber.nn
import net.liplum.lib.Serialized
import net.liplum.lib.segmentLines
import net.liplum.lib.utils.toFloat
import net.liplum.mdt.CalledBySync
import net.liplum.mdt.render.G
import net.liplum.mdt.render.Text
import net.liplum.mdt.ui.bars.AddBar
import net.liplum.mdt.utils.PackedPos
import net.liplum.mdt.utils.build
import kotlin.math.max

class DataCDN(name: String) : Block(name) {
    @JvmField var maxLink = 3
    @JvmField var linkRange = 500f

    init {
        update = true
        solid = true
        configurable = true
        envEnabled = envEnabled or Env.space
        config(Integer::class.java) { b: CdnBuild, i ->
            b.linkCommandNodeFromRemote(i.toInt())
        }
        configClear { b: CdnBuild ->
            b.clearLinks()
        }
    }

    override fun init() {
        super.init()
        clipSize = max(clipSize, linkRange * 1.2f)
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            AddBar<CdnBuild>("network",
                { data.network.toString() },
                { Pal.power },
                { networkGraph.entity.isAdded.toFloat() }
            )
        }
    }

    companion object {
        val tempNodes = ArrayList<INetworkNode>()
    }

    inner class CdnBuild : Building(), INetworkNode {
        @Serialized
        override var data = NetworkModule()
        @CalledBySync
        fun linkCommandNodeFromRemote(pos: PackedPos) {
            val target = pos.nn() ?: return
            val contains = data.links.contains(pos)
            if (contains) {
                // unlink it
                unlink(target)
            } else if (canLink(target)) {
                // link it
                link(target)
            }
        }

        fun canLink(target: INetworkNode): Boolean {
            if (data.links.size >= maxLink) return false
            return linkRange < 0 || target.building.dst(this) <= linkRange
        }

        override fun created() {
            super.created()
            networkGraph.add(this)
        }

        override fun draw() {
            super.draw()
            DebugOnly {
                for (i in 0 until data.links.size) {
                    val link = data.links[i]
                    val linkB = link.build
                    if (linkB != null)
                        G.drawDashLineBetweenTwoBlocksBreath(this.tile, linkB.tile)
                }

                Text.drawTextEasy(
                    "[red]${data.network.id}[]\n${data.links};\n${networkGraph.nodes}".segmentLines(50), x, y, Color.white
                )
            }
        }

        fun updateNetwork() {
            for (other in getNetworkConnections(tempNodes)) {
                other.networkGraph.addNetwork(this.networkGraph)
            }
        }

        override fun onProximityAdded() {
            super.onProximityAdded()
            updateNetwork()
        }

        override fun drawSelect() {
            super.drawSelect()
            G.circle(x, y, linkRange)
        }

        override fun drawConfigure() {
            super.drawConfigure()
            G.circle(x, y, linkRange)
        }
        @CalledBySync
        fun clearLinks() {
            data.links.clear()
        }

        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            val node = other.nn()
            if (node != null) {
                configure(other.pos())
                return false
            }
            return true
        }
    }
}