package net.liplum.data

import arc.func.Prov
import mindustry.Vars.world
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.meta.Env
import net.liplum.DebugOnly
import net.liplum.api.cyber.*
import net.liplum.api.cyber.SideLinks.Companion.enableAllSides
import net.liplum.lib.Serialized
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.G
import net.liplum.mdt.utils.NewEmptyPos
import kotlin.math.max

class DataCDN(name: String) :
    Block(name), INetworkBlock {
    override var linkRange = 500f
    override val block = this
    @ClientOnly @JvmField var expendingPlacingLineTimePreRange = 60f / 500f
    @ClientOnly override var expendPlacingLineTime = -1f
    override val sideEnable = enableAllSides

    init {
        buildType = Prov { CdnBuild() }
        update = true
        solid = true
        envEnabled = envEnabled or Env.space
        initNetworkNodeSettings()
    }

    override fun init() {
        super.init()
        clipSize = max(clipSize, linkRange * 1.2f)
        if (expendPlacingLineTime < 0f)
            expendPlacingLineTime = expendingPlacingLineTimePreRange * linkRange
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        drawPlaceCardinalDirections(x, y)
    }

    companion object {
        val tempNodes = ArrayList<INetworkNode>()
    }

    inner class CdnBuild : Building(),
        INetworkNode {
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
        override var network = DataNetwork()
        override var init: Boolean = false
        override var links = SideLinks()
        override val sideEnable = this@DataCDN.sideEnable
        @ClientOnly
        override val expendSelectingLineTime = this@DataCDN.expendPlacingLineTime
        override val linkRange = this@DataCDN.linkRange
        var lastTileChange = -2
        override fun updateTile() {
            if (lastTileChange != world.tileChanges) {
                lastTileChange = world.tileChanges
                updateCardinalDirections()
            }
        }

        override fun created() {
            super.created()
            network.add(this)
        }

        override fun draw() {
            super.draw()
            DebugOnly {
                drawLinkInfo()
            }
        }

        override fun drawSelect() {
            super.drawSelect()
            drawSelectingCardinalDirections()
            G.circle(x, y, linkRange)
            DebugOnly {
                drawNetworkInfo()
            }
        }

        override fun drawConfigure() {
            super.drawConfigure()
            G.circle(x, y, linkRange)
        }

        override fun onRemoved() {
            super.onRemoved()
            onRemoveFromGround()
        }

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            onRemoveFromGround()
        }

        override fun afterPickedUp() {
            super.afterPickedUp()
            onRemoveFromGround()
        }

        override fun toString() = "DataCDN#$id"
    }
}