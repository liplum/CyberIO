package net.liplum.data

import arc.func.Prov
import arc.graphics.g2d.Draw
import arc.scene.ui.layout.Table
import mindustry.Vars
import mindustry.content.UnitTypes
import mindustry.gen.BlockUnitc
import mindustry.graphics.Layer
import mindustry.world.blocks.ControlBlock
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock
import mindustry.world.meta.Env
import net.liplum.DebugOnly
import net.liplum.Var
import net.liplum.api.cyber.*
import net.liplum.api.cyber.SideLinks.Companion.enableAllSides
import net.liplum.common.shader.use
import net.liplum.input.smoothSelect
import net.liplum.registry.SD
import plumy.animation.ContextDraw.Draw
import plumy.core.ClientOnly
import plumy.core.MUnit
import plumy.core.Serialized
import plumy.core.assets.TR
import plumy.dsl.WorldXY
import plumy.dsl.sprite

class Serializer(name: String) :
    PayloadBlock(name), INetworkBlock {
    var serializationSpeed = 1f / 240f
    override var linkRange: WorldXY = 64f * Vars.tilesize
    override val block = this
    @ClientOnly override var expendPlacingLineTime = Var.SelectedCircleTime
    override val sideEnable = enableAllSides
    /** how much data it can store. This number will be increased by one to prevent from blocking transfer */
    override var dataCapacity = 3
    @ClientOnly @JvmField var railTR = TR()
    @ClientOnly @JvmField var rialEndTR = TR()

    init {
        outputsPayload = false
        acceptsPayload = true
        update = true
        rotate = false
        size = 3
        payloadSpeed = 1.2f
        envEnabled = envEnabled or Env.space
        //make sure to display large units.
        clipSize = 120f
        buildType = Prov { SerializerBuild() }
        setupNetworkNodeSettings()
    }

    override fun load() {
        super.load()
        railTR.set("power-beam".sprite)
        rialEndTR.set("power-beam-end".sprite)
    }

    override fun init() {
        dataCapacity += 1 // to prevent from blocking transfer
        initNetworkNodeSettings()
        super.init()
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            addSendingProgress<SerializerBuild>()
        }
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        drawPlaceCardinalDirections(x, y)
    }

    inner class SerializerBuild : PayloadBlockBuild<Payload>(),
        INetworkNode, ControlBlock {
        @Serialized
        override var network = DataNetwork()
        override var init: Boolean = false
        override var links = SideLinks()
        @Serialized
        override var request: DataID = EmptyDataID
        @Serialized
        override var dataInSending: DataID = EmptyDataID
        @Serialized
        override val dataList = PayloadDataList(dataCapacity)
        @Serialized
        override var currentOriented: Side = -1
        @Serialized
        var serializingProgress = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        override var totalSendingDistance = 0f
        override var curSendingLength = 0f
        @ClientOnly
        override val expendSelectingLineTime = this@Serializer.expendPlacingLineTime
        override val linkRange = this@Serializer.linkRange
        override val sideEnable = this@Serializer.sideEnable
        @ClientOnly
        override val linkingTime = FloatArray(4)
        override var livingTime = 0f
        @ClientOnly
        override val lastRailTrail = Array(4) { RailTrail() }
        var lastTileChange = -2
        override fun draw() {
            DebugOnly {
                drawLinkInfo()
                drawPayloadList()
            }
            Draw.rect(region, x, y)
            //draw input
            for (i in 0..3) {
                if (blends(i)) {
                    Draw.rect(inRegion, x, y, (i * 90 - 180).toFloat())
                }
            }
            Draw.rect(topRegion, x, y)
            Draw.z(Layer.blockOver)
            val payload = payload
            if (payload != null) {
                if (hasArrived()) {
                    updatePayload()
                    Draw.z(Layer.blockOver)
                    if (serializingProgress > 0f) {
                        SD.Vanishing.use {
                            it.region.set(payload.icon())
                            it.progress = serializingProgress
                            payload.icon().Draw(x, y)
                        }
                    } else {
                        payload.icon().Draw(x, y)
                    }
                } else {
                    drawPayload()
                }
            }
            drawRail(railTR, rialEndTR)
            drawCurrentDataInSending()
        }

        val canAddMoreData: Boolean
            get() = dataList.size < dataList.capacity - 1

        override fun updateTile() {
            if (lastTileChange != Vars.world.tileChanges) {
                lastTileChange = Vars.world.tileChanges
                updateCardinalDirections()
            }
            updateAsNode()
            // Don't update payload
            moveInPayload(false)
            val payload = payload
            if (!hasArrived() || !canAddMoreData || payload == null) return
            serializingProgress += delta() * serializationSpeed
            if (serializingProgress >= 1f) {
                this.payload = null
                addData(PayloadData(payload, DataNetwork.assignDataID()))
                serializingProgress = 0f
            }
        }

        override fun buildConfiguration(table: Table) {
            buildNetworkDataList(table)
        }

        override fun drawSelect() {
            super.drawSelect()
            drawSelectingCardinalDirections()
            drawRangeCircle(alpha = smoothSelect(expendSelectingLineTime))
            DebugOnly {
                drawNetworkInfo()
            }
        }

        override fun created() {
            super.created()
            network.initNode(this)
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

        var unit = UnitTypes.block.create(team) as BlockUnitc
        override fun unit(): MUnit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as MUnit)
        }

        override fun toString() = "Serializer#$id"
    }
}