package net.liplum.holo

import arc.func.Floatf
import arc.graphics.Color
import arc.scene.ui.layout.Table
import arc.struct.Seq
import arc.util.Structs
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.type.Item
import mindustry.type.Liquid
import mindustry.type.UnitType
import mindustry.ui.Styles
import mindustry.world.Block
import mindustry.world.blocks.ItemSelection
import mindustry.world.consumers.ConsumeItemDynamic
import mindustry.world.meta.BlockGroup
import net.liplum.CalledBySync
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.liquidCons.DynamicLiquidCons
import net.liplum.registries.CioLiquids.cyberion
import net.liplum.ui.bars.AddBar
import net.liplum.utils.ID
import net.liplum.utils.ItemTypeAmount
import net.liplum.utils.bundle
import kotlin.math.max

open class HoloProjector(name: String) : Block(name) {
    @JvmField var plans: Seq<HoloPlan> = Seq()
    @JvmField var itemCapabilities: IntArray = IntArray(0)
    @JvmField var cyberionCapacity: Float = 0f

    init {
        solid = true
        update = true
        hasPower = true
        hasItems = true
        hasLiquids = true
        group = BlockGroup.units
        rotate = true
        configurable = true
        sync = true
        config(Integer::class.java) { obj: HoloPBuild, plan ->
            obj.setPlan(plan.toInt())
        }

        consumes.add(ConsumeItemDynamic<HoloPBuild> {
            it.curPlan.itemReqs
        })
        consumes.add(DynamicLiquidCons.create<HoloPBuild> {
            it.curPlan.cyberionReq
        })
    }

    override fun init() {
        itemCapabilities = IntArray(ItemTypeAmount())
        for (plan in plans) {
            for (itemReq in plan.itemReqs) {
                itemCapabilities[itemReq.item.ID] =
                    max(
                        itemCapabilities[itemReq.item.id.toInt()],
                        itemReq.amount * 2
                    )
                itemCapacity = max(itemCapacity, itemReq.amount * 2)
            }
        }
        cyberionCapacity = plans.max(
            Floatf { it.req.cyberionReq }
        ).req.cyberionReq * 2
        liquidCapacity = cyberionCapacity
        super.init()
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            AddBar<HoloPBuild>(R.Bar.ProgressN,
                { R.Bar.Progress.bundle(progress) },
                { Pal.bar },
                { progress }
            )
        }
    }

    protected val Int.plan: HoloPlan?
        get() = if (this < 0 || this >= plans.size)
            null
        else
            plans[this]

    open inner class HoloPBuild : Building() {
        var planOrder: Int = -1
        val curPlan: HoloPlan?
            get() = planOrder.plan
        var progressTime = 0f
        val progress: Float
            get() {
                val plan = curPlan
                return if (plan != null)
                    progressTime / plan.time
                else
                    0f
            }

        override fun updateTile() {
            val itemsModule = items
            if (!consValid()) return
            val plan = curPlan ?: return
            progressTime += delta()

            if (progressTime >= plan.time) {
                consume()
                projectUnit(plan.unitType)
                progressTime = 0f
            }
        }
        @CalledBySync
        open fun setPlan(plan: Int) {
            var order = plan
            if (order < 0 || order >= plans.size) {
                order = -1
            }
            if (order == planOrder) return
            planOrder = order
            progressTime = 0f
        }

        override fun buildConfiguration(table: Table) {
            val options = Seq.with(plans).map {
                it.unitType
            }.filter {
                it.unlockedNow() && !it.isBanned
            }
            if (options.any()) {
                ItemSelection.buildTable(this@HoloProjector, table, options,
                    { curPlan?.unitType }
                ) { unit: UnitType? ->
                    val selected = plans.indexOf {
                        it.unitType == unit
                    }
                    configure(selected)
                }
            } else {
                table.table(Styles.black3) { t: Table ->
                    t.add("@none").color(Color.lightGray)
                }
            }
        }

        override fun onConfigureTileTapped(other: Building): Boolean {
            if (this === other) {
                deselect()
                configure(null)
                return false
            }
            return true
        }

        open fun projectUnit(unitType: UnitType) {
            unitType.spawn(team, this)
        }

        override fun acceptLiquid(source: Building, liquid: Liquid) =
            liquid == cyberion && liquids[cyberion] < cyberionCapacity

        override fun getMaximumAccepted(item: Item) =
            itemCapabilities[item.id.toInt()]

        override fun acceptItem(source: Building, item: Item): Boolean {
            val curPlan = curPlan ?: return false
            return items[item] < getMaximumAccepted(item) &&
                    Structs.contains(curPlan.req.items) {
                        it.item === item
                    }
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
        }

        override fun write(write: Writes) {
            super.write(write)
        }
    }
}