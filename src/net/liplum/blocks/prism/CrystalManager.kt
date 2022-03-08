package net.liplum.blocks.prism

import arc.math.Mathf
import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import net.liplum.persistance.readSeq
import net.liplum.persistance.writeSeq

enum class Status {
    Shrinking, Expending
}
internal typealias Obelisk = PrismObelisk.ObeliskBuild

class CrystalManager(
    maxAmount: Int = 3,
    initCapacity: Int = maxAmount
) {
    lateinit var prism: Prism.PrismBuild
    var maxAmount: Int = maxAmount
        set(value) {
            field = value.coerceAtLeast(0)
        }
    @JvmField var crystals: Seq<Crystal> = Seq(
        initCapacity + Mathf.log2(initCapacity.toFloat()).toInt()
    )
    @JvmField var obelisks: Seq<Obelisk> = Seq(
        maxAmount - 1
    )
    val orbitedAmount: Int
        get() = crystals.size
    var validAmount: Int = 0
        set(value) {
            field = value.coerceAtLeast(0)
        }
    var expendRequirement = 60f
    var curExpendTime = 0f
        set(value) {
            field = value.coerceIn(0f, expendRequirement)
        }
    val process: Float
        get() = curExpendTime / expendRequirement
    val canAdd: Boolean
        get() = validAmount < maxAmount
    val canRemove: Boolean
        get() = validAmount > 0
    val canLinkAnyObelisk: Boolean
        get() = canAdd
    val obeliskCount: Int
        get() = obelisks.size
    @JvmField var status = Status.Shrinking
    val Crystal.isInOrbit: Boolean
        get() = !this.isAwaitAdding
    val Crystal.canReallyBeRemovedNow: Boolean
        get() = this.isRemoved && this.revolution.r <= 0f
    val anyInQueue: Boolean
        get() {
            for (crystal in crystals) {
                if (crystal.canReallyBeRemovedNow || crystal.isAwaitAdding) {
                    return true
                }
            }
            return false
        }

    fun spend(delta: Float) {
        if (anyInQueue) {
            status = Status.Expending
        }
        when (status) {
            Status.Expending -> curExpendTime += delta
            Status.Shrinking -> curExpendTime -= delta
        }
        if (process >= 0.999f) {
            retrieveAll()
            releaseAll()
            status = Status.Shrinking
            curExpendTime = 0f
        }
    }

    fun tryLink(obelisk: Obelisk): Boolean {
        if (canLinkAnyObelisk) {
            obelisks.add(obelisk)
            return true
        }
        return false
    }

    fun unlinkAllObelisks() {
        obelisks.forEach {
            it.linked = null
        }
    }

    fun removeNonexistentObelisk() {
        obelisks.removeAll {
            it.tile.build != it || it.linked != prism
        }
    }

    fun updateObelisk() {
        val shouldExistCount = obeliskCount + 1
        for (i in 0 until validAmount - shouldExistCount) {
            tryRemoveOutermost()
        }
    }

    inline fun tryAddNew(init: Crystal.() -> Unit): Boolean {
        if (canAdd) {
            crystals.add(Crystal().apply {
                orbitPos = validAmount
                isAwaitAdding = true
            }.apply(init))
            validAmount++
            status = Status.Expending
            return true
        }
        return false
    }

    fun findFirstRemovableOutermost(): Crystal? {
        for (i in crystals.size - 1 downTo 0) {
            val crystal = crystals[i]
            if (!crystal.isRemoved) {
                return crystal
            }
        }
        return null
    }

    fun tryRemoveOutermost(): Boolean {
        if (canRemove) {
            val needRemoved = findFirstRemovableOutermost()
            if (needRemoved != null) {
                needRemoved.isRemoved = true
                validAmount--
                status = Status.Expending
            }
            return true
        }
        return false
    }

    fun releaseAll() {
        crystals.forEach {
            if (it.isAwaitAdding) {
                it.isAwaitAdding = false
            }
        }
    }

    fun retrieveAll() {
        crystals.removeAll { it.canReallyBeRemovedNow }
    }

    inline fun render(render: Crystal.() -> Unit) {
        for (crystal in crystals) {
            if (crystal.isInOrbit) {
                crystal.render()
            }
        }
    }

    inline fun update(update: Crystal.() -> Unit) {
        for (crystal in crystals) {
            if (crystal.isInOrbit) {
                crystal.update()
            }
        }
    }

    companion object {
        @JvmStatic
        fun Writes.write(cm: CrystalManager) {
            this.writeSeq(cm.crystals, Crystal::write)
            this.writeSeq(cm.obelisks) { write, it ->
                write.i(it.pos())
            }
            this.f(cm.curExpendTime)
            this.b(cm.validAmount)
        }
        @JvmStatic
        fun Reads.read(): CrystalManager {
            val cm = CrystalManager()
            cm.crystals = this.readSeq(Crystal::read)
            cm.obelisks = this.readSeq {
                Vars.world.build(it.i()) as Obelisk
            }
            cm.curExpendTime = this.f()
            cm.validAmount = this.b().toInt()
            return cm
        }
    }
}