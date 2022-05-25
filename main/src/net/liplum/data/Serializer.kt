package net.liplum.data

import arc.audio.Sound
import arc.graphics.g2d.Draw
import arc.util.Time
import mindustry.content.Fx
import mindustry.entities.Effect
import mindustry.gen.Sounds
import mindustry.gen.Unit
import mindustry.graphics.Layer
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock
import net.liplum.lib.shaders.SD
import net.liplum.lib.shaders.use

class Serializer(name: String) : PayloadBlock(name) {
    var incinerateEffect: Effect = Fx.blastExplosion
    var incinerateSound: Sound = Sounds.bang

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

    inner class SerializerBuild : PayloadBlockBuild<Payload>() {
        override fun draw() {
            SD.Hologramize.use {
                it.region = block.region
                it.progress = (Time.time % 120f) / 120f
                it.offset = 0.1f
                Draw.rect(region, x, y)
                //draw input
                for (i in 0..3) {
                    if (blends(i)) {
                        Draw.rect(inRegion, x, y, (i * 90 - 180).toFloat())
                    }
                }
                Draw.rect(topRegion, x, y)
                Draw.z(Layer.blockOver)
                drawPayload()
            }
        }

        override fun acceptUnitPayload(unit: Unit?): Boolean {
            return true
        }

        override fun updateTile() {
            super.updateTile()
            if (moveInPayload(false) && efficiency > 0) {
                payload = null
                incinerateEffect.at(this)
                incinerateSound.at(this)
            }
        }
    }
}