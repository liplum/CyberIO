package net.liplum.mixin

import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.geom.Vec2
import arc.util.Tmp
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.world.Block
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock
import net.liplum.common.persistence.*
import java.io.DataInputStream

interface PayloadBlockMixin<T : Payload> {
    var payload: Payload?
    var payVec2: Vec2
    var payRotation: Float
    var curried: Boolean
    val self: Building
    val selfBlock: Block
        get() = self.block
    val payloadSpeed: Float
    val payloadRotateSpeed: Float
    fun updatePayload() {
        payload?.set(self.x + payVec2.x, self.y + payVec2.y, payRotation)
    }

    fun blends(direction: Int): Boolean {
        return PayloadBlock.blends(self, direction)
    }
    /** @return true if the payload is in position.
     */
    fun moveInPayload(): Boolean = moveInPayload(true)
    /** @return true if the payload is in position.
     */
    fun moveInPayload(rotate: Boolean): Boolean {
        if (payload == null) return false
        updatePayload()
        if (rotate) {
            payRotation = Angles.moveToward(
                payRotation,
                if (self.block.rotate) self.rotdeg() else 90f,
                payloadRotateSpeed * self.delta()
            )
        }
        payVec2.approach(Vec2.ZERO, payloadSpeed * self.delta())
        return hasArrived()
    }

    fun moveOutPayload() {
        val payload = payload ?: return
        val delta = self.delta()
        val selfSize = selfBlock.size
        updatePayload()
        val dest = Tmp.v1.trns(self.rotdeg(), selfSize * Vars.tilesize / 2f)
        payRotation = Angles.moveToward(payRotation, self.rotdeg(), payloadRotateSpeed * delta)
        payVec2.approach(dest, payloadSpeed * delta)
        val front: Building? = self.front()
        val canDump = front == null || !front.tile().solid()
        val canMove = front != null && (front.block.outputsPayload || front.block.acceptsPayload)
        if (canDump && !canMove) {
            PayloadBlock.pushOutput(payload, 1f - payVec2.dst(dest) / (selfSize * Vars.tilesize / 2f))
        }
        if (payVec2.within(dest, 0.001f)) {
            payVec2.clamp(
                -selfSize * Vars.tilesize / 2f,
                -selfSize * Vars.tilesize / 2f,
                selfSize * Vars.tilesize / 2f,
                selfSize * Vars.tilesize / 2f
            )
            if (canMove) {
                if (self.movePayload(payload)) {
                    this.payload = null
                }
            } else if (canDump) {
                dumpPayload()
            }
        }
    }

    fun hasArrived(): Boolean {
        return payVec2.isZero(0.01f)
    }

    fun dumpPayload() {
        //translate payload forward slightly
        val payload = payload ?: return
        val tx = Angles.trnsx(payload.rotation(), 0.1f)
        val ty = Angles.trnsy(payload.rotation(), 0.1f)
        payload[payload.x() + tx, payload.y() + ty] = payload.rotation()
        if (payload.dump()) {
            this.payload = null
        } else {
            payload[payload.x() - tx, payload.y() - ty] = payload.rotation()
        }
    }

    fun drawPayload() {
        val payload = payload
        if (payload != null) {
            updatePayload()
            Draw.z(Layer.blockOver)
            payload.draw()
        }
    }

    fun writePayload(writer: Writes) {
        payVec2.write(writer)
        writer.f(payRotation)
        Payload.write(payload, writer)
    }

    fun readPayload(reader: Reads) {
        payVec2.read(reader)
        payRotation = reader.f()
        payload = Payload.read(reader)
    }

    fun writePayload(writer: CacheWriter) {
        payVec2.write(writer)
        writer.f(payRotation)
        writer.Wrap {
            Payload.write(payload, this)
        }
    }

    fun readPayload(_reader_: DataInputStream) = CacheReaderSpec(_reader_).run {
        payVec2.read(this)
        payRotation = this.f()
        this.Warp {
            payload = Payload.read(this)
        }
    }
}