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
import net.liplum.lib.shaders.use
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.Draw
import net.liplum.mdt.utils.NewEmptyPos
import net.liplum.mdt.utils.WorldXY
import net.liplum.mdt.utils.worldXY
import net.liplum.registries.SD

class Serializer(name: String) :
    PayloadBlock(name), INetworkBlock {
    var serializationSpeed = 1f / 240f
    override var linkRange: WorldXY = 500f
    override val block = this
    @ClientOnly override var expendPlacingLineTime = Var.selectedCircleTime
    override val sideEnable = enableAllSides

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
        initNetworkNodeSettings()
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
        override val data = PayloadData()
        @Serialized
        override val currentOriented = NewEmptyPos()
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
        override var routine: DataNetwork.Path? = null
        override val linkRange = this@Serializer.linkRange
        override val sideEnable = this@Serializer.sideEnable
        var lastTileChange = -2
        override fun draw() {
            DebugOnly {
                drawLinkInfo()
                val data = data.data
                if (data != null) {
                    data.set(x, y + size.worldXY, payloadRotation)
                    data.draw()
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
        }

        override fun updateTile() {
            if (lastTileChange != Vars.world.tileChanges) {
                lastTileChange = Vars.world.tileChanges
                updateCardinalDirections()
            }
            // Don't update payload
            moveInPayload(false)
            val payload = payload
            if (!hasArrived() || !data.isEmpty || payload == null) return
            serializingProgress += delta() * serializationSpeed
            if (serializingProgress >= 1f) {
                this.payload = null
                data.data = payload
                serializingProgress = 0f
            }
        }

        override fun buildConfiguration(table: Table) {
            buildNetworkDataList(table)
        }

        override fun drawSelect() {
            super.drawSelect()
            DebugOnly {
                drawNetworkInfo()
            }
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

        override fun toString() = "Serializer#$id"
    }
}