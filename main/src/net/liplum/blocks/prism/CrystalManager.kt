package net.liplum.blocks.prism

import arc.math.Mathf
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.mdt.ClientOnly
import net.liplum.lib.persistance.intSet
import net.liplum.lib.persistance.readSeq
import net.liplum.lib.persistance.writeSeq
import net.liplum.mdt.utils.TE
import net.liplum.mdt.utils.build
import net.liplum.mdt.utils.exists

enum class Status {
    Shrinking, Expending
}
internal typealias Obelisk = PrismObelisk.ObeliskBuild

open class CrystalManager(
    maxAmount: Int = 3,
) {
    var initCrystalCount: Int = 1
    lateinit var addCrystalCallback: Crystal.() -> Unit
    @ClientOnly
    var genCrystalImgCallback: (Crystal.() -> Unit)? = null
    lateinit var prism: Prism.PrismBuild
    var maxAmount: Int = maxAmount
        set(value) {
            field = value.coerceAtLeast(0)
        }
    @JvmField var crystals: Seq<Crystal> = Seq(
        maxAmount + Mathf.log2(maxAmount.toFloat()).toInt()
    )
    @JvmField var obelisks: OrderedSet<Int> = OrderedSet(
        maxAmount - 1
    )
    val inOrbitAmount: Int
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
    @JvmField var inited: Boolean = false
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
    val canReleaseMore: Boolean
        get() = inOrbitAmount <= validAmount

    fun spend(delta: Float) {
        var anyInQueue = false
        for (crystal in crystals) {
            if (crystal.isAwaitAdding || crystal.isRemoved) {
                anyInQueue = true
                break
            }
        }
        status = if (anyInQueue) {
            Status.Expending
        } else {
            Status.Shrinking
        }

        when (status) {
            Status.Expending -> curExpendTime += delta
            Status.Shrinking -> curExpendTime -= delta
        }
        if (process >= 0.999f && anyInQueue) {
            retrieveAll()
            if (canReleaseMore) {
                releaseAll()
            }
            status = Status.Shrinking
        }
    }

    fun tryLink(obelisk: Obelisk): Boolean {
        if (canLinkAnyObelisk) {
            obelisks.add(obelisk.pos())
            obelisk.link(prism)
            tryAddNew()
            return true
        }
        return false
    }

    fun unlinkAllObelisks() {
        obelisks.forEach {
            it.TE<Obelisk>()?.unlink()
        }
    }

    fun removeNonexistentObelisk() {
        obelisks.removeAll {
            if (it == null) {
                false
            } else {
                val build = it.build
                !build.exists || (build as? Obelisk)?.linked != prism.pos()
            }
        }
    }

    fun updateObelisk() {
        val shouldExistCount = obeliskCount + 1
        for (i in 0 until validAmount - shouldExistCount) {
            tryRemoveOutermost()
        }
    }

    fun findFirstInOrbit(pos: Int): Crystal? {
        crystals.forEach {
            if (it.orbitPos == pos)
                return it
        }
        return null
    }

    fun tryAddNew(): Boolean {
        if (canAdd) {
            val orbitPos = validAmount
            val stillAlive = findFirstInOrbit(orbitPos)
            if (stillAlive != null && stillAlive.isRemoved) {
                stillAlive.isRemoved = false
            } else {
                val crystal = Crystal().apply {
                    this.orbitPos = orbitPos
                    isAwaitAdding = true
                    addCrystalCallback()
                }
                crystals.add(crystal)
                ClientOnly {
                    genCrystalImgCallback?.let { crystal.it() }
                }
                status = Status.Expending
            }
            validAmount++
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
        fun CrystalManager.write(writes: Writes) {
            writes.writeSeq(crystals, Crystal::write)
            writes.intSet(obelisks)
            writes.f(curExpendTime)
            writes.b(validAmount)
            writes.b(status.ordinal)
        }
        @JvmStatic
        fun CrystalManager.read(read: Reads) {
            crystals = read.readSeq(Crystal::read)
            genCrystalImgCallback?.let { crystals.forEach(it) }
            obelisks = read.intSet()
            curExpendTime = read.f()
            validAmount = read.b().toInt()
            status = Status.values()[read.b().toInt()]
        }
    }
}