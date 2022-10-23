package net.liplum.mixin

import arc.Events
import arc.math.Mathf
import arc.scene.ui.layout.Table
import arc.struct.Seq
import arc.util.Tmp
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.content.Fx
import mindustry.core.World
import mindustry.entities.EntityGroup
import mindustry.entities.Units
import mindustry.game.EventType.PayloadDropEvent
import mindustry.game.EventType.PickupEvent
import mindustry.gen.Building
import mindustry.gen.Payloadc
import mindustry.gen.Unit
import mindustry.gen.Unitc
import mindustry.io.TypeIO
import mindustry.type.UnitType
import mindustry.world.Build
import mindustry.world.blocks.payloads.BuildPayload
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.UnitPayload
import mindustry.world.blocks.power.PowerGraph
import plumy.core.math.sqr
import plumy.core.MUnit

interface PayloadMixin : Payloadc, PowerGraphc {
    val unitType: UnitType
    var payloads: Seq<Payload>
    override fun payloadUsed(): Float =
        payloads.sumf { it.size().sqr }

    override fun dropUnit(payload: UnitPayload): Boolean {
        val u = payload.unit
        if (!u.canPass(tileX(), tileY()) || Units.count(
                x, y, u.physicSize()
            ) { o: Unit -> o.isGrounded } > 1
        ) {
            return false
        }
        Fx.unitDrop.at(this)
        if (Vars.net.client()) return true
        u.set(this)
        u.trns(Tmp.v1.rnd(Mathf.random(2.0f)))
        u.rotation(rotation())
        u.id = EntityGroup.nextId()
        if (!u.isAdded) u.team.data().updateCount(u.type, -1)
        u.add()
        u.unloaded()
        Events.fire(PayloadDropEvent(self(), u))
        return true
    }

    override fun addPayload(payload: Payload) {
        payloads.add(payload)
    }

    override fun payloads(): Seq<Payload> = payloads
    override fun payloads(new: Seq<Payload>) {
        payloads = new
    }

    override fun tryDropPayload(payload: Payload): Boolean {
        val on = tileOn()
        if (Vars.net.client() && payload is UnitPayload) {
            Vars.netClient.clearRemovedEntity(payload.unit.id)
        }
        if (on != null && on.build?.acceptPayload(on.build, payload) == true) {
            Fx.unitDrop.at(on.build)
            on.build.handlePayload(on.build, payload)
            return true
        }
        if (payload is BuildPayload) {
            return dropBlock(payload)
        }
        if (payload is UnitPayload) {
            return dropUnit(payload)
        }
        return false
    }

    override fun pickup(tile: Building) {
        tile.pickedUp()
        tile.tile.remove()
        tile.afterPickedUp()
        addPayload(BuildPayload(tile))
        Fx.unitPickup.at(tile)
        Events.fire(PickupEvent(self(), tile))
    }

    override fun pickup(unit: MUnit) {
        unit.remove()
        addPayload(UnitPayload(unit))
        Fx.unitPickup.at(unit)
        if (Vars.net.client()) {
            Vars.netClient.clearRemovedEntity(unit.id)
        }
        Events.fire(PickupEvent(self(), unit))
    }

    override fun canPickup(build: Building): Boolean =
        payloadUsed() + build.block.size.sqr * Vars.tilePayload <= unitType.payloadCapacity + 0.001f &&
                build.canPickup()

    override fun canPickup(unit: Unit): Boolean = unitType.pickupUnits &&
            payloadUsed() + unit.hitSize.sqr <= unitType.payloadCapacity + 0.001f &&
            unit.team == team() &&
            unit.isAI

    override fun canPickupPayload(pay: Payload): Boolean =
        payloadUsed() + pay.size().sqr <= unitType.payloadCapacity + 0.001f &&
                (unitType.pickupUnits || pay !is UnitPayload)

    override fun dropLastPayload(): Boolean {
        if (payloads.isEmpty) return false
        val load = payloads.peek()
        return if (tryDropPayload(load)) {
            payloads.pop()
            true
        } else false
    }

    override fun contentInfo(table: Table, itemSize: Float, width: Float) {
        table.clear()
        table.top().left()
        var pad = 0f
        val items = payloads.size.toFloat()
        if (itemSize * items + pad * items > width) {
            pad = (width - itemSize * items) / items
        }
        for (p in payloads) {
            table.image(p.icon()).size(itemSize).padRight(pad)
        }
    }

    override fun hasPayload() = payloads.size > 0
    override fun dropBlock(payload: BuildPayload): Boolean {
        val tile = payload.build
        val tx = World.toTile(this.x - tile.block.offset)
        val ty = World.toTile(this.y - tile.block.offset)
        val on = Vars.world.tile(tx, ty)
        return if (on != null &&
            Build.validPlace(tile.block, tile.team, tx, ty, tile.rotation, false)
        ) {
            val rot = ((this.rotation() + 45.0f) / 90.0f).toInt() % 4
            payload.place(on, rot)
            Events.fire(PayloadDropEvent(this as MUnit, tile))
            val controllerName = (this as Unitc).controllerName
            if (controllerName != null) {
                payload.build.lastAccessed = controllerName
            }
            Fx.unitDrop.at(tile)
            Fx.placeBlock.at(on.drawx(), on.drawy(), on.block().size.toFloat())
            true
        } else false
    }

    fun updatePayload() {
        payloadPower?.clear()
        for (pay in payloads) {
            if (pay is BuildPayload && pay.build.power != null) {
                val payPower = payloadPower ?: PowerGraph()
                payloadPower = payPower
                pay.build.power.graph = null
                payPower.add(pay.build)
            }
        }
        payloadPower?.update()
        for (pay in payloads) {
            pay.set(x, y, rotation())
            pay.update(self(), null)
        }
    }

    fun writePayload(write: Writes) {
        write.i(payloads.size)
        for (payload in payloads) {
            TypeIO.writePayload(write, payload)
        }
    }

    fun readPayload(read: Reads) {
        val payloadSize = read.i()
        payloads.clear()
        for (INDEX in 0 until payloadSize) {
            val payloadItem = TypeIO.readPayload(read)
            if (payloadItem != null)
                payloads.add(payloadItem)
        }
    }
}