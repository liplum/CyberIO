package net.liplum.blocks.gadgets

import arc.graphics.g2d.Draw
import arc.struct.OrderedSet
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.type.Item
import mindustry.world.Tile
import mindustry.world.consumers.ConsumeItems
import mindustry.world.consumers.ConsumeType
import mindustry.world.meta.BlockGroup
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.config
import net.liplum.api.data.CyberU
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender
import net.liplum.blocks.AniedBlock
import net.liplum.delegates.Delegate
import net.liplum.persistance.intSet
import net.liplum.utils.*

private typealias AniStateD = AniState<SmartDistributor, SmartDistributor.SmartDISBuild>

open class SmartDistributor(name: String) : AniedBlock<SmartDistributor, SmartDistributor.SmartDISBuild>(name) {
    @ClientOnly lateinit var RequireAni: AniStateD
    @ClientOnly lateinit var NoPowerAni: AniStateD
    @JvmField var maxConnection = -1
    @ClientOnly lateinit var NoPowerTR: TR

    init {
        solid = true
        update = true
        hasItems = true
        itemCapacity = 50
        hasPower = true
        group = BlockGroup.transportation
        noUpdateDisabled = true
        canOverdrive = false
        unloadable = false
    }

    override fun load() {
        super.load()
        NoPowerTR = this.inMod("rs-no-power")
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            bars.addAniStateInfo<SmartDISBuild>()
        }
    }

    open inner class SmartDISBuild : AniedBlock<SmartDistributor, SmartDistributor.SmartDISBuild>.AniedBuild(),
        IDataReceiver {
        @JvmField var requirements: Array<Item> = Item::class.java.EmptyArray()
        var senders = OrderedSet<Int>()
        var onRequirementUpdated: Delegate = Delegate()
        @ClientOnly var lastDistributionTime = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }

        override fun drawSelect() {
            G.init()

            G.drawSurroundingCircle(tile, R.C.Cloud)

            CyberU.drawSenders(this, senders)
        }

        open fun updateRequirements() {
            val all = HashSet<Item>()
            for (build in proximity) {
                val consumes = build.block.consumes
                if (consumes.has(ConsumeType.item)) {
                    val reqs: ConsumeItems = consumes[ConsumeType.item]
                    for (req in reqs.items) {
                        all.add(req.item)
                    }
                }
            }
            requirements = if (all.isEmpty()) {
                Item::class.java.EmptyArray()
            } else {
                all.toTypedArray()
            }
            onRequirementUpdated()
        }

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            updateRequirements()
        }

        override fun updateTile() {
            if (consValid()) {
                val dised = distribute()
                if (dised) {
                    lastDistributionTime = 0f
                }
            }
            lastDistributionTime += Time.delta
        }

        open fun distribute(): Boolean {
            if (!block.hasItems || items.total() == 0) {
                return false
            }
            var dised = false
            for (b in proximity) {
                if (b.team == team) {
                    val consumes = b.block.consumes
                    if (consumes.has(ConsumeType.item)) {
                        val reqs: ConsumeItems = consumes[ConsumeType.item]
                        for (req in reqs.items) {
                            val item = req.item
                            if (items.has(item) && b.acceptItem(this, item)) {
                                b.handleItem(this, item)
                                items.remove(item, 1)
                                dised = true
                                break
                            }
                        }
                    }
                }
            }
            return dised
        }
        /*override fun beforeFixedDraw() {
            Draw.rect(BaseTR, x, y)
            Draw.color(Color.brown)
            Draw.rect(BaffleTR, x, y)
        }
        */
        override fun receiveData(sender: IDataSender, item: Item, amount: Int) {
            items.add(item, amount)
        }

        override fun acceptedAmount(sender: IDataSender, item: Item): Int {
            return if (item in requirements)
                getMaximumAccepted(item) - items[item]
            else
                0
        }
        @ClientOnly
        override fun canAcceptAnyData(sender: IDataSender) =
            !power.status.isZero() && requirements.isNotEmpty() && !isBlocked

        override fun getRequirements(): Array<Item>? = requirements
        @ClientOnly
        override fun isBlocked() = lastDistributionTime > 30f
        override fun connect(sender: IDataSender) {
            senders.add(sender.building.pos())
        }

        override fun disconnect(sender: IDataSender) {
            senders.remove(sender.building.pos())
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            senders = read.intSet()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.intSet(senders)
        }

        override fun connectedSenders() = senders
        override fun connectedSender(): Int? = senders.first()
        override fun acceptConnection(sender: IDataSender) =
            if (maxConnection == -1) true else senders.size < maxConnection

        override fun getBuilding(): SmartDISBuild = this
        override fun getTile(): Tile = this.tile
        override fun getBlock() = this@SmartDistributor
    }

    override fun genAniConfig() {
        config {
            From(NoPowerAni) To RequireAni When {
                !it.power.status.isZero()
            }
            From(RequireAni) To NoPowerAni When {
                it.power.status.isZero()
            }
        }
    }

    override fun genAniState() {
        RequireAni = addAniState("Require")
        NoPowerAni = addAniState("NoPower") {
            Draw.rect(NoPowerTR, it.x, it.y)
        }
    }
}