package net.liplum.blocks.gadgets

import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.scene.ui.Label
import arc.struct.OrderedSet
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.type.Item
import mindustry.type.ItemStack
import mindustry.world.consumers.Consume
import mindustry.world.consumers.ConsumeItemDynamic
import mindustry.world.consumers.ConsumeItems
import mindustry.world.consumers.ConsumeType
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.UndebugOnly
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.animations.anims.AnimationObj
import net.liplum.lib.animations.anis.AniState
import net.liplum.lib.Draw
import net.liplum.lib.animations.anis.config
import net.liplum.lib.delegates.Delegate1
import net.liplum.lib.ui.bars.addBar
import net.liplum.lib.ui.bars.removeItems
import net.liplum.persistance.intSet
import net.liplum.utils.*
import kotlin.math.log2

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
    @JvmField var boost2Count: (Float) -> Int = {
        if (it <= 1.1f)
            1
        else if (it in 1.1f..2.1f)
            2
        else if (it in 2.1f..3f)
            3
        else
            Mathf.round(log2(it + 5.1f))
    }

    init {
        solid = true
        update = true
        hasItems = true
        itemCapacity = 50
        hasPower = true
        group = BlockGroup.transportation
        noUpdateDisabled = true
        canOverdrive = true
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
        stats.remove(Stat.powerUse)
        stats.add(Stat.powerUse) {
            val l = Label("$contentType.$name.stats.power-use".bundle)
            it.add(l)
        }
    }

    override fun setBars() {
        super.setBars()
        UndebugOnly {
            bars.removeItems()
        }
        DebugOnly {
            addBar("dis-count", {
                "Count:" + boost2Count(timeScale)
            }, {
                Pal.power
            }, {
                boost2Count(timeScale) / 4f
            })
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

        init {
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
                val dised = DoMultipleBool(canOverdrive, boost2Count(timeScale), this::distribute)
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
            if (proximity.isEmpty) return false
            disIndex %= proximity.size
            val b = proximity[disIndex]
            if (b.team == team) {
                val consumes = b.block.consumes
                if (consumes.has(ConsumeType.item)) {
                    val reqs: Consume = consumes[ConsumeType.item]
                    if (reqs is ConsumeItems) {
                        dised = distributeTo(b, reqs.items)
                    } else if (reqs is ConsumeItemDynamic) {
                        dised = distributeTo(b, reqs.items.get(b))
                    }
                }
            }
            disIndex++
            return dised
        }

        protected open fun distributeTo(other: Building, reqs: Array<ItemStack>): Boolean {
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
            this.drawRequirements()
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

        override fun getConnectedSenders() = senders
        override fun maxSenderConnection() = maxConnection
    }

    override fun genAniConfig() {
        config {
            From(NoPowerAni) To DistributingAni When {
                !power.status.isZero
            }
            From(DistributingAni) To NoPowerAni When {
                power.status.isZero
            }
        }
    }

    override fun genAniState() {
        DistributingAni = addAniState("Distributing")
        NoPowerAni = addAniState("NoPower") {
            NoPowerTR.Draw(x, y)
        }
    }
}