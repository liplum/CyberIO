package net.liplum.blocks.gadgets

import arc.graphics.g2d.Draw
import arc.struct.OrderedSet
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.type.Item
import mindustry.type.ItemStack
import mindustry.world.Tile
import mindustry.world.consumers.Consume
import mindustry.world.consumers.ConsumeItemDynamic
import mindustry.world.consumers.ConsumeItems
import mindustry.world.consumers.ConsumeType
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.UndebugOnly
import net.liplum.animations.anims.Animation
import net.liplum.animations.anims.AnimationObj
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.DrawTR
import net.liplum.animations.anis.config
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender
import net.liplum.api.drawDataNetGraphic
import net.liplum.api.drawLinkedLineToReceiverWhenConfiguring
import net.liplum.api.whenNotConfiguringSender
import net.liplum.blocks.AniedBlock
import net.liplum.delegates.Delegate1
import net.liplum.persistance.intSet
import net.liplum.ui.bars.removeItems
import net.liplum.utils.*

private typealias AniStateD = AniState<SmartDistributor, SmartDistributor.SmartDISBuild>

open class SmartDistributor(name: String) : AniedBlock<SmartDistributor, SmartDistributor.SmartDISBuild>(name) {
    @ClientOnly lateinit var DistributingAni: AniStateD
    @ClientOnly lateinit var NoPowerAni: AniStateD
    @JvmField var maxConnection = -1
    @ClientOnly lateinit var NoPowerTR: TR
    @ClientOnly lateinit var ArrowsAnim: Animation
    @JvmField @ClientOnly var ArrowsAnimFrames = 9
    @JvmField @ClientOnly var ArrowsAnimDuration = 70f
    @JvmField @ClientOnly var DistributionTime = 60f
    @JvmField var DynamicReqUpdateTime = 30f
    @JvmField var powerUsagePerItem = 2.5f
    @JvmField var powerUsageBasic = 3f

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
        allowConfigInventory = false
        sync = true
    }

    override fun init() {
        consumes.powerDynamic<SmartDISBuild> {
            it._requirements.size * powerUsagePerItem + powerUsageBasic
        }
        super.init()
    }

    override fun load() {
        super.load()
        NoPowerTR = this.inMod("rs-no-power")
        ArrowsAnim = this.autoAnim("arrows", ArrowsAnimFrames, ArrowsAnimDuration)
    }

    override fun setStats() {
        super.setStats()
        stats.add(Stat.powerUse, powerUsageBasic * 60f, StatUnit.powerSecond)
    }

    override fun setBars() {
        super.setBars()
        UndebugOnly {
            bars.removeItems()
        }
        DebugOnly {
            bars.addSenderInfo<SmartDISBuild>()
        }
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        this.drawLinkedLineToReceiverWhenConfiguring(x, y)
    }

    open inner class SmartDISBuild : AniedBlock<SmartDistributor, SmartDistributor.SmartDISBuild>.AniedBuild(),
        IDataReceiver {
        @JvmField var _requirements: Array<Item> = Item::class.java.EmptyArray()
        var senders = OrderedSet<Int>()
        @JvmField var _onRequirementUpdated: Delegate1<IDataReceiver> = Delegate1()
        override fun getOnRequirementUpdated() = _onRequirementUpdated
        @ClientOnly var lastDistributionTime = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @ClientOnly lateinit var arrowsAnimObj: AnimationObj
        @ClientOnly open val isDistributing: Boolean
            get() = lastDistributionTime < DistributionTime
        var hasDynamicRequirements: Boolean = false
        var dynamicReqUpdateTimer = 0f
        var disIndex = 0
        override fun created() {
            super.created()
            ClientOnly {
                arrowsAnimObj = ArrowsAnim.gen()
            }
        }

        override fun onRemoved() {
            _onRequirementUpdated.clear()
        }

        open fun updateRequirements() {
            val all = HashSet<Item>()
            hasDynamicRequirements = false
            for (build in proximity) {
                val consumes = build.block.consumes
                if (consumes.has(ConsumeType.item)) {
                    val reqs: Consume = consumes[ConsumeType.item]
                    if (reqs is ConsumeItems) {
                        for (req in reqs.items) {
                            all.add(req.item)
                        }
                    } else if (reqs is ConsumeItemDynamic) {
                        for (req in reqs.items.get(build)) {
                            all.add(req.item)
                        }
                        hasDynamicRequirements = true
                    }
                }
            }
            val newReqs = if (all.isEmpty()) {
                Item::class.java.EmptyArray()
            } else {
                all.toTypedArray()
            }
            if (!newReqs.equalsNoOrder(_requirements)) {
                _requirements = newReqs
                DebugOnly {
                    requirementsText = genRequirementsText()
                }
                _onRequirementUpdated(this)
            }
        }

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            updateRequirements()
        }

        override fun onProximityAdded() {
            super.onProximityAdded()
            updateRequirements()
        }

        override fun updateTile() {
            if (hasDynamicRequirements) {
                dynamicReqUpdateTimer += Time.delta
                if (dynamicReqUpdateTimer >= DynamicReqUpdateTime) {
                    dynamicReqUpdateTimer = 0f
                    updateRequirements()
                }
            } else {
                dynamicReqUpdateTimer = 0f
            }
            if (consValid()) {
                val dised = distribute()
                if (dised) {
                    lastDistributionTime = 0f
                }
            }
            lastDistributionTime += delta()
        }

        open fun distribute(): Boolean {
            if (!block.hasItems || items.total() == 0) {
                return false
            }
            var dised = false
            if(proximity.isEmpty) return false
            disIndex %= proximity.size
            val b = proximity[disIndex]
            if (b.team == team) {
                val consumes = b.block.consumes
                if (consumes.has(ConsumeType.item)) {
                    val reqs: Consume = consumes[ConsumeType.item]
                    if (reqs is ConsumeItems) {
                        dised = distributeFunc(b, reqs.items)
                    } else if (reqs is ConsumeItemDynamic) {
                        dised = distributeFunc(b, reqs.items.get(b))
                    }
                }
            }
            disIndex++
            return dised
        }

        protected open fun distributeFunc(other: Building, reqs: Array<ItemStack>): Boolean {
            for (req in reqs) {
                val item = req.item
                if (items.has(item) && other.acceptItem(this, item)) {
                    other.handleItem(this, item)
                    items.remove(item, 1)
                    return true
                }
            }
            return false
        }

        override fun receiveData(sender: IDataSender, item: Item, amount: Int) {
            if (this.isConnectedWith(sender)) {
                items.add(item, amount)
            }
        }

        override fun acceptedAmount(sender: IDataSender, item: Item): Int {
            if (!consValid()) return 0

            return if (item in _requirements)
                getMaximumAccepted(item) - items[item]
            else
                0
        }

        override fun getRequirements(): Array<Item>? = _requirements
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
            disIndex = read.b().toInt()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.intSet(senders)
            write.b(disIndex)
        }
        @CioDebugOnly
        var requirementsText: String = ""
        @CioDebugOnly
        fun genRequirementsText() = _requirements.genText()
        override fun drawSelect() {
            whenNotConfiguringSender {
                this.drawDataNetGraphic()
            }
            DebugOnly {
                if (requirementsText.isNotEmpty()) {
                    drawPlaceText(requirementsText, tileX(), tileY(), true)
                }
            }
        }

        override fun beforeDraw() {
            arrowsAnimObj.spend(delta())
            if (isDistributing) {
                arrowsAnimObj.wakeUp()
            } else {
                arrowsAnimObj.sleep()
            }
        }

        override fun fixedDraw() {
            super.fixedDraw()
            Draw.color(team.color)
            arrowsAnimObj.draw(x, y)
        }

        override fun connectedSenders() = senders
        override fun connectedSender(): Int? = senders.first()
        override fun acceptConnection(sender: IDataSender) =
            if (maxConnection == -1) true else senders.size < maxConnection

        override fun maxSenderConnection() = maxConnection
        override fun getBuilding(): SmartDISBuild = this
        override fun getTile(): Tile = this.tile
        override fun getBlock() = this@SmartDistributor
    }

    override fun genAniConfig() {
        config {
            From(NoPowerAni) To DistributingAni When {
                !it.power.status.isZero()
            }
            From(DistributingAni) To NoPowerAni When {
                it.power.status.isZero()
            }
        }
    }

    override fun genAniState() {
        DistributingAni = addAniState("Distributing")
        NoPowerAni = addAniState("NoPower") {
            DrawTR(NoPowerTR, it.x, it.y)
        }
    }
}