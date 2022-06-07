package net.liplum.data

import arc.func.Prov
import arc.scene.ui.layout.Table
import mindustry.Vars.world
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.meta.Env
import net.liplum.DebugOnly
import net.liplum.api.cyber.*
import net.liplum.api.cyber.SideLinks.Companion.enableAllSides
import net.liplum.lib.Serialized
import net.liplum.lib.TR
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.G
import net.liplum.mdt.render.smoothSelect
import net.liplum.mdt.utils.atlas
import net.liplum.mdt.utils.worldXY
import net.liplum.utils.addSendingProgress
import kotlin.math.max

class DataCDN(name: String) :
    Block(name), INetworkBlock {
    override var linkRange = 500f
    override val block = this
    @ClientOnly @JvmField var expendingPlacingLineTimePreRange = 60f / 500f
    @ClientOnly override var expendPlacingLineTime = -1f
    override val sideEnable = enableAllSides
    override var dataCapacity = 2
    @ClientOnly @JvmField var railTR = TR()
    @ClientOnly @JvmField var railEndTR = TR()

    init {
        solid = true
        buildType = Prov { CdnBuild() }
        envEnabled = envEnabled or Env.space
        setupNetworkNodeSettings()
    }

    override fun init() {
        initNetworkNodeSettings()
        super.init()
        clipSize = max(clipSize, linkRange * 1.2f)
        if (expendPlacingLineTime < 0f)
            expendPlacingLineTime = expendingPlacingLineTimePreRange * linkRange
    }

    override fun load() {
        super.load()
        railTR.set("power-beam".atlas())
        railEndTR.set("power-beam-end".atlas())
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        drawPlaceCardinalDirections(x, y)
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            addSendingProgress<CdnBuild>()
        }
    }

    inner class CdnBuild : Building(),
        INetworkNode {
        // TODO: Serialization
        @Serialized
        override val dataList = PayloadDataList(dataCapacity)
        @Serialized
        override var currentOriented: Side = -1
        @Serialized
        override var request: DataID = EmptyDataID
        @Serialized
        override var dataBeingSent: DataID = EmptyDataID
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
        override val linkingTime = FloatArray(4)
        @ClientOnly
        override val expendSelectingLineTime = this@DataCDN.expendPlacingLineTime
        override val linkRange = this@DataCDN.linkRange
        var lastTileChange = -2
        override fun updateTile() {
            if (lastTileChange != world.tileChanges) {
                lastTileChange = world.tileChanges
                updateCardinalDirections()
            }
            updateAsNode()
        }

        override fun created() {
            super.created()
            network.initNode(this)
        }

        override fun buildConfiguration(table: Table) {
            buildNetworkDataListSelector(table)
        }

        override fun draw() {
            super.draw()
            DebugOnly {
                drawLinkInfo()
                if (dataList.isNotEmpty) {
                    val cur = dataList.first()
                    cur.payload.set(x, y + size.worldXY, payloadRotation)
                    cur.payload.draw()
                }
            }
            drawRail(railTR, railEndTR)
        }

        override fun drawSelect() {
            super.drawSelect()
            drawSelectingCardinalDirections()
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
            onRemovedFromGround()
        }
        override fun onProximityAdded() {
            super.onProximityAdded()
            updateCardinalDirections()
        }
        override fun onProximityRemoved() {
            super.onProximityRemoved()
            onRemovedFromGround()
        }

        override fun afterPickedUp() {
            super.afterPickedUp()
            onRemovedFromGround()
        }

        override fun toString() = "DataCDN#$id"
    }
}