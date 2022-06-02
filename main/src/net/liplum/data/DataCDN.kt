package net.liplum.data

import arc.func.Prov
import mindustry.Vars.world
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.meta.Env
import net.liplum.DebugOnly
import net.liplum.api.cyber.*
import net.liplum.lib.Serialized
import net.liplum.lib.utils.toFloat
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.G
import net.liplum.mdt.ui.bars.AddBar
import net.liplum.mdt.utils.NewEmptyPos
import kotlin.math.max

class DataCDN(name: String) :
    Block(name), INetworkBlock {
    override var maxLink = 4
    override var linkRange = 500f
    override val block = this
    @ClientOnly @JvmField var expendingPlacingLineTimePreRange = 60f / 500f
    @ClientOnly override var expendPlacingLineTime = -1f

    init {
        buildType = Prov { CdnBuild() }
        update = true
        solid = true
        envEnabled = envEnabled or Env.space
        initDataNetworkRemoteConfig()
    }

    override fun init() {
        super.init()
        clipSize = max(clipSize, linkRange * 1.2f)
        if (expendPlacingLineTime < 0f)
            expendPlacingLineTime = expendingPlacingLineTimePreRange * linkRange
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            AddBar<CdnBuild>("network",
                { dataMod.network.toString() },
                { Pal.power },
                { networkGraph.entity.isAdded.toFloat() }
            )
        }
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        drawPlaceCardinalDirections(x, y)
    }

    companion object {
        val tempNodes = ArrayList<INetworkNode>()
    }

    inner class CdnBuild : Building(),
        ISideNetworkNode {
        // TODO: Serialization
        @Serialized
        override val data = PayloadData()
        @Serialized
        override val currentOriented = NewEmptyPos()
        @Serialized
        override var routine: DataNetwork.Path? = null
        @Serialized
        override var sendingProgress = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        @Serialized
        override var dataMod = NetworkModule()
        override val sideLinks = IntArray(4) { -1 }
        override val linkRange = this@DataCDN.linkRange
        override val maxLink = this@DataCDN.maxLink
        var lastTileChange = -2
        override fun updateTile() {
            if (lastTileChange != world.tileChanges) {
                lastTileChange = world.tileChanges
                updateCardinalDirections()
            }
        }

        override fun created() {
            super.created()
            networkGraph.add(this)
        }

        override fun draw() {
            super.draw()
            DebugOnly {
                drawNetworkInfo()
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
        override fun onRemoved() {
            super.onRemoved()
            onRemovedInWorld()
        }

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            onRemovedInWorld()
        }

        override fun afterPickedUp() {
            super.afterPickedUp()
            onRemovedInWorld()
        }

        override fun toString() = "DataCDN#$id"
    }
}