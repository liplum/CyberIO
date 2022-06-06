package net.liplum.data

import arc.graphics.g2d.Draw
import arc.scene.ui.layout.Table
import mindustry.Vars
import mindustry.graphics.Layer
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock
import mindustry.world.meta.Env
import net.liplum.DebugOnly
import net.liplum.Var
import net.liplum.api.cyber.*
import net.liplum.api.cyber.SideLinks.Companion.enableAllSides
import net.liplum.lib.Serialized
import net.liplum.lib.TR
import net.liplum.lib.shaders.use
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.Draw
import net.liplum.mdt.render.smoothSelect
import net.liplum.mdt.utils.WorldXY
import net.liplum.mdt.utils.atlas
import net.liplum.mdt.utils.worldXY
import net.liplum.registries.SD
import net.liplum.utils.addSendingProgress

class Serializer(name: String) :
    PayloadBlock(name), INetworkBlock {
    var serializationSpeed = 1f / 240f
    override var linkRange: WorldXY = 500f
    override val block = this
    @ClientOnly override var expendPlacingLineTime = Var.SelectedCircleTime
    override val sideEnable = enableAllSides
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
        setupNetworkNodeSettings()
    }

    override fun load() {
        super.load()
        railTR.set("power-beam".atlas())
        rialEndTR.set("power-beam-end".atlas())
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
        INetworkNode {
        @Serialized
        override var network = DataNetwork()
        override var init: Boolean = false
        override var links = SideLinks()
        @Serialized
        override var request: DataID = EmptyDataID
        @Serialized
        override val dataList = PayloadDataList(dataCapacity)
        @Serialized
        override var currentOriented: Side = -1
        @Serialized
        override var sendingProgress = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        @Serialized
        var serializingProgress = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        @ClientOnly
        override val expendSelectingLineTime = this@Serializer.expendPlacingLineTime
        override val linkRange = this@Serializer.linkRange
        override val sideEnable = this@Serializer.sideEnable
        @ClientOnly
        override val warmUp = FloatArray(4)
        var lastTileChange = -2
        override fun draw() {
            DebugOnly {
                drawLinkInfo()
                if (dataList.isNotEmpty) {
                    val cur = dataList.first()
                    cur.payload.set(x, y + size.worldXY, payloadRotation)
                    cur.payload.draw()
                }
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
        }

        override fun updateTile() {
            if (lastTileChange != Vars.world.tileChanges) {
                lastTileChange = Vars.world.tileChanges
                updateCardinalDirections()
            }
            updateAsNode()
            // Don't update payload
            moveInPayload(false)
            val payload = payload
            if (!hasArrived() || !dataList.canAddMore || payload == null) return
            serializingProgress += delta() * serializationSpeed
            if (serializingProgress >= 1f) {
                this.payload = null
                dataList.add(PayloadData(payload, DataNetwork.assignDataID()))
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

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            onRemovedFromGround()
        }

        override fun afterPickedUp() {
            super.afterPickedUp()
            onRemovedFromGround()
        }

        override fun toString() = "Serializer#$id"
    }
}