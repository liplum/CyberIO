package net.liplum.data

import arc.Core
import arc.graphics.g2d.Draw
import mindustry.graphics.Layer
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock
import net.liplum.api.cyber.INetworkNode
import net.liplum.api.cyber.NetworkModule
import net.liplum.lib.Serialized
import net.liplum.lib.shaders.use
import net.liplum.mdt.render.Draw
import net.liplum.mdt.utils.NewEmptyPos
import net.liplum.registries.SD

class Serializer(name: String) : PayloadBlock(name) {
    var serializationSpeed = 1f / 240f

    init {
        outputsPayload = false
        acceptsPayload = true
        update = true
        rotate = false
        size = 3
        payloadSpeed = 1.2f
        //make sure to display large units.
        clipSize = 120f
    }

    inner class SerializerBuild : PayloadBlockBuild<Payload>(),
        INetworkNode {
        @Serialized
        override var dataMod = NetworkModule()
        @Serialized
        override val data = PayloadData()
        @Serialized
        override val currentOriented = NewEmptyPos()
        @Serialized
        override var sendingProgress = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        override var routine: DataNetwork.Path? = null
        @Serialized
        var serializingProgress = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }

        override fun draw() {
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
                updatePayload()
                Draw.z(Layer.blockOver)
                SD.Vanishing.use {
                    it.region.set(payload.icon())
                    it.progress = serializingProgress
                    payload.icon().Draw(x, y)
                }
            }
        }

        override fun updateTile() {
            // Don't update payload
            moveInPayload(false)
            val payload = payload
            if (!hasArrived() || payload == null) return
            //if (!hasArrived() || !data.isEmpty || payload == null) return
            serializingProgress += delta() * serializationSpeed
            if (serializingProgress >= 1f) {
                this.payload = null
                data.data = payload
            }
        }
    }
}