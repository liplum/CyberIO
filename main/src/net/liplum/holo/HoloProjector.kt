package net.liplum.holo

import arc.Events
import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.TextureRegion
import arc.math.geom.Vec2
import arc.scene.ui.layout.Table
import arc.struct.Seq
import arc.util.Eachable
import arc.util.Strings.autoFixed
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.entities.units.BuildPlan
import mindustry.game.EventType.UnitCreateEvent
import mindustry.gen.Building
import mindustry.gen.Icon
import mindustry.gen.Iconc
import mindustry.gen.Tex
import mindustry.graphics.Pal
import mindustry.logic.LAccess
import mindustry.type.*
import mindustry.ui.Fonts
import mindustry.ui.Styles
import mindustry.world.Block
import mindustry.world.consumers.ConsumeItemDynamic
import mindustry.world.draw.DrawBlock
import mindustry.world.draw.DrawDefault
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.UndebugOnly
import net.liplum.common.util.percentI
import net.liplum.utils.CalledBySync
import net.liplum.utils.ServerOnly
import net.liplum.consumer.ConsumeFluidDynamic
import net.liplum.ui.addItemSelectorDefault
import net.liplum.ui.bars.removeItemsInBar
import net.liplum.utils.ItemTypeAmount
import net.liplum.utils.sub
import net.liplum.registry.CioFluid.cyberion
import net.liplum.ui.addTable
import plumy.core.ClientOnly
import plumy.core.Else
import plumy.core.MUnit
import plumy.core.Serialized
import plumy.core.arc.Tick
import plumy.core.assets.TRs
import plumy.core.math.approach
import plumy.core.math.approachDelta
import plumy.dsl.*
import kotlin.math.max

open class HoloProjector(name: String) : Block(name) {
    @JvmField var plans: ArrayList<HoloPlan> = ArrayList()
    @JvmField var itemCapabilities: IntArray = IntArray(0)
    @JvmField var holoUnitCapacity = 8
    @JvmField var warmupSpeed = 0.015f
    @JvmField var preparingSpeed = 0.015f
    @ClientOnly @JvmField var projectingSpeed = 2f
    @JvmField var drawer: DrawBlock = DrawDefault()

    init {
        buildType = Prov { HoloProjectorBuild() }
        solid = true
        update = true
        hasPower = true
        hasItems = true
        updateInUnits = true
        alwaysUpdateInUnits = true
        hasLiquids = true
        group = BlockGroup.units
        saveConfig = true
        configurable = true
        sync = true
        generateIcons = false
        commandable = true
    }

    override fun init() {
        consume(ConsumeFluidDynamic<HoloProjectorBuild> {
            val plan = it.curPlan ?: return@ConsumeFluidDynamic LiquidStack.empty
            plan.liquidArray
        })
        consume(ConsumeItemDynamic<HoloProjectorBuild> {
            val plan = it.curPlan ?: return@ConsumeItemDynamic ItemStack.empty
            plan.items
        })
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
        val cyberionCapacity = plans.maxBy { it.cyberion * 60f }.cyberion * 2
        liquidCapacity = max(liquidCapacity, cyberionCapacity)
        config<HoloProjectorBuild, PackedPos> {
            setPlan(it)
        }
        configNull<HoloProjectorBuild> {
            setPlan(-1)
        }
        if (plans.size == 1) configurable = false
        super.init()
    }

    val Int.plan: HoloPlan?
        get() = if (this < 0 || this >= plans.size) null
        else plans[this]
    var hoveredInfo: Table? = null
    override fun load() {
        super.load()
        drawer.load(this)
    }

    override fun loadIcon() {
        super.loadIcon()
        fullIcon = sub("preview")
        uiIcon = fullIcon
    }

    override fun icons(): TRs = drawer.finalIcons(this)
    override fun getRegionsToOutline(out: Seq<TextureRegion>) =
        drawer.getRegionsToOutline(this, out)

    override fun drawPlanRegion(plan: BuildPlan, list: Eachable<BuildPlan>) {
        super.drawPlanRegion(plan, list)
        drawer.drawPlan(this, plan, list)
    }

    open inner class HoloProjectorBuild : Building() {
        override fun version() = 1.toByte()
        @Serialized
        var warmup = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        @Serialized
        var preparing = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        @Serialized
        var planIndex: Int = -1
        val curPlan: HoloPlan?
            get() = if (configurable) planIndex.plan else 0.plan
        @Serialized
        var progressTime = 0f
        @ClientOnly
        var projecting = 0f
        var commandPos: Vec2? = null
        override fun block(): HoloProjector = this@HoloProjector
        val progress: Float
            get() {
                val plan = curPlan
                return if (plan != null) (progressTime / plan.time).coerceIn(0f, 1f)
                else 0f
            }

        override fun updateTile() {
            val plan = curPlan
            if (plan != null && efficiency > 0f) {
                val delta = edelta()
                preparing = preparing.approach(1f, preparingSpeed * delta)
                if (preparing >= 1f) {
                    warmup = warmup.approach(1f, warmupSpeed * delta)
                    if (warmup >= 1f) progressTime += delta
                }
                if (progressTime >= plan.time) {
                    val unitType = plan.unitType
                    if (unitType.canCreateHoloUnitIn(team)) {
                        projectUnit(unitType)
                        consume()
                        progressTime = 0f
                    }
                } else {
                    if (warmup >= 1f) projecting += delta * projectingSpeed
                }
            } else {
                warmup = warmup.approachDelta(0f, warmupSpeed)
                preparing = preparing.approachDelta(0f, preparingSpeed)
            }
        }
        @CalledBySync
        open fun setPlan(plan: Int) {
            var order = plan
            if (order < 0 || order >= plans.size) {
                order = -1
            }
            if (order == planIndex) return
            planIndex = order
            val p = curPlan
            progressTime = if (p != null)
                progressTime.coerceAtMost(p.time)
            else
                0f
            rebuildHoveredInfo()
        }

        override fun shouldConsume() :Boolean{
            val plan = curPlan?:return false
            return enabled && ( progress < 1f || plan.unitType.canCreateHoloUnitIn(team))
        }
        override fun buildConfiguration(table: Table) {
            val options = Seq.with(plans).map {
                it.unitType
            }.retainAll {
                it.unlockedNow() && !it.isBanned
            }
            if (options.any()) {
                table.addItemSelectorDefault(this@HoloProjector, options,
                    { curPlan?.unitType }
                ) { unit: UnitType? ->
                    val selected = plans.indexOfFirst {
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

        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            return true
        }

        open fun rebuildHoveredInfo() {
            try {
                val info = hoveredInfo
                if (info != null) {
                    info.clear()
                    display(info)
                }
            } catch (_: Exception) {
                // Maybe null pointer or cast exception
            }
        }

        @JvmField var lastUnitInPayload: MUnit? = null
        fun findTrueHoloProjectorSource(): HoloProjectorBuild {
            val unit = lastUnitInPayload
            if (unit is HoloUnit) {
                val trueProjector = unit.projectorPos.castBuild<HoloProjectorBuild>()
                if (trueProjector != null)
                    return trueProjector
            }
            return this
        }

        override fun updatePayload(unitHolder: MUnit?, buildingHolder: Building?) {
            lastUnitInPayload = unitHolder
            super.updatePayload(unitHolder, buildingHolder)
        }

        override fun config(): Any? = planIndex
        open fun projectUnit(unitType: HoloUnitType) {
            val unit = unitType.create(team)
            if (unit is HoloUnit) {
                unit.set(x, y)
                ServerOnly {
                    unit.add()
                }
                unit.setProjector(findTrueHoloProjectorSource())
                val commandPos = commandPos
                if (commandPos != null && unit.isCommandable) {
                    unit.command().commandPosition(commandPos)
                }
                Events.fire(UnitCreateEvent(unit, this))
            }
        }

        override fun draw() = drawer.draw(this)
        override fun acceptLiquid(source: Building, liquid: Liquid) =
            liquid == cyberion && liquids[cyberion] < liquidCapacity

        override fun getMaximumAccepted(item: Item) =
            itemCapabilities[item.id.toInt()]

        override fun acceptItem(source: Building, item: Item): Boolean {
            val curPlan = curPlan ?: return false
            return items[item] < getMaximumAccepted(item) && item in curPlan
        }

        override fun drawLight() {
            super.drawLight()
            drawer.drawLight(this)
        }

        override fun created() {
            team.updateHoloCapacity(this)
        }

        override fun add() {
            super.add()
            team.updateHoloCapacity(this)
        }

        override fun updateProximity() {
            super.updateProximity()
            team.updateHoloCapacity(this)
        }

        override fun remove() {
            super.remove()
            team.updateHoloCapacity()
        }

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            team.updateHoloCapacity()
        }
        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            planIndex = read.b().toInt()
            progressTime = read.f()
            val version = revision.toInt()
            if (version >= 1) {
                warmup = read.f()
                preparing = read.f()
            }
        }

        override fun write(write: Writes) {
            super.write(write)
            write.b(planIndex)
            write.f(progressTime)
            write.f(warmup)
            write.f(preparing)
        }

        override fun senseObject(sensor: LAccess): Any? {
            return when (sensor) {
                LAccess.config -> planIndex
                else -> super.sense(sensor)
            }
        }

        override fun sense(sensor: LAccess): Double {
            return when (sensor) {
                LAccess.progress -> progress.toDouble()
                else -> super.sense(sensor)
            }
        }

        override fun getCommandPosition(): Vec2? {
            return commandPos
        }

        override fun onCommand(target: Vec2) {
            commandPos = target
        }

        override fun warmup() = warmup
        override fun totalProgress() = this.projecting
        override fun progress() = progress
    }

    override fun setBars() {
        super.setBars()
        UndebugOnly {
            removeItemsInBar()
        }
        DebugOnly {
            AddBar<HoloProjectorBuild>("progress",
                { "${"bar.progress".bundle}: ${progress.percentI}" },
                { Var.Hologram },
                { progress }
            )
        }.Else {
            AddBar<HoloProjectorBuild>("progress",
                { "bar.progress".bundle },
                { Var.Hologram },
                { progress }
            )
        }
        AddBar<HoloProjectorBuild>(R.Bar.Vanilla.UnitsN,
            {
                val curPlan = curPlan
                if (curPlan == null)
                    "[lightgray]${Iconc.cancel}"
                else {
                    val unitType = curPlan.unitType
                    R.Bar.Vanilla.UnitCapacity.bundle(
                        Fonts.getUnicodeStr(unitType.name),
                        team.data().countType(unitType),
                        team.getStringHoloCap()
                    )
                }
            },
            { Pal.power },
            {
                val curPlan = curPlan
                curPlan?.unitType?.pctOfTeamOwns(team) ?: 0f
            }
        )
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.itemCapacity)

        stats.add(Stat.output) { stat: Table ->
            stat.row()
            for (plan in plans) {
                stat.addTable {
                    background = Tex.whiteui
                    setColor(Pal.darkestGray)
                    if (plan.unitType.isBanned) {
                        image(Icon.cancel).color(Pal.remove).size(40f)
                        return@addTable
                    }
                    if (plan.unitType.unlockedNow()) {
                        image(plan.unitType.uiIcon).size(40f).pad(10f).left()
                        addTable {
                            add(plan.unitType.localizedName).left()
                            row()
                            add("${autoFixed(plan.time / 60f, 1)} ${"unit.seconds".bundle}")
                                .color(Color.lightGray)
                        }.left()
                        addTable {
                            right()
                            add(autoFixed(plan.req.cyberion * plan.time, 1))
                                .color(cyberion.color).padLeft(12f).left()
                            image(cyberion.uiIcon).size((8 * 3).toFloat())
                                .padRight(2f).right()
                        }.right().grow().pad(10f)
                    } else {
                        image(Icon.lock).color(Pal.darkerGray).size(40f)
                    }
                }.growX().pad(5f)
                stat.row()
            }
        }
    }
}

class HoloPlanningSpec(
    val projector: HoloProjector,
) {
    infix fun HoloUnitType.needs(req: HoloPlanRequirement) {
        projector.plans += HoloPlan(this, req)
    }

    fun those(
        cyberion: Float = 0f,
        item: ItemStack? = null,
        items: Array<ItemStack>? = null,
        time: Tick,
    ) = if (items != null) HoloPlanRequirement(cyberion, items, time)
    else if (item != null) HoloPlanRequirement(cyberion, arrayOf(item), time)
    else HoloPlanRequirement(cyberion, emptyArray(), time)
}

inline fun HoloProjector.planning(
    config: HoloPlanningSpec.() -> Unit,
) {
    HoloPlanningSpec(this).config()
}