package net.liplum.holo

import arc.Events
import arc.func.Floatf
import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.scene.ui.layout.Table
import arc.struct.Seq
import arc.util.Strings.autoFixed
import arc.util.Structs
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.game.EventType.UnitCreateEvent
import mindustry.gen.Building
import mindustry.gen.Iconc
import mindustry.gen.Unit
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.logic.LAccess
import mindustry.type.Item
import mindustry.type.Liquid
import mindustry.type.UnitType
import mindustry.ui.Fonts
import mindustry.ui.Styles
import mindustry.world.Block
import mindustry.world.consumers.ConsumeItemDynamic
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.S
import net.liplum.UndebugOnly
import net.liplum.lib.Serialized
import net.liplum.common.shaders.use
import net.liplum.common.utils.bundle
import net.liplum.common.utils.percentI
import net.liplum.mdt.*
import net.liplum.mdt.consumer.DynamicContinuousLiquidCons
import net.liplum.mdt.render.Draw
import net.liplum.mdt.ui.addItemSelectorDefault
import net.liplum.mdt.ui.bars.AddBar
import net.liplum.mdt.ui.bars.removeItemsInBar
import net.liplum.mdt.utils.*
import net.liplum.registries.CioLiquids.cyberion
import net.liplum.registries.SD
import kotlin.math.max

open class HoloProjector(name: String) : Block(name) {
    @JvmField var plans: Seq<HoloPlan> = Seq()
    @JvmField var itemCapabilities: IntArray = IntArray(0)
    @JvmField var cyberionCapacity: Float = 0f
    @JvmField var holoUnitCapacity = 8
    @JvmField var powerUse = 3f
    @ClientOnly @JvmField var projectorShrink = 5f
    @ClientOnly @JvmField var projectorCenterRate = 3f
    /**
     * For vertices of plan
     */
    @ClientOnly
    val vecs = arrayOf(Vec2(), Vec2(), Vec2(), Vec2())

    init {
        buildType = Prov { HoloPBuild() }
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
        commandable = true
        config(Integer::class.java) { obj: HoloPBuild, plan ->
            obj.setPlan(plan.toInt())
        }
        configClear { obj: HoloPBuild ->
            obj.setPlan(-1)
        }
    }

    override fun init() {
        consume(ConsumeItemDynamic<HoloPBuild> {
            it.curPlan.itemReqs
        })

        consume(object : DynamicContinuousLiquidCons({
            (it as HoloPBuild).curPlan.cyberionReq
        }) {
            override fun update(b: Building) {
                b as HoloPBuild
                val plan = b.curPlan
                if (plan != null) {
                    val liquid = plan.req.liquid
                    if (liquid != null) {
                        b.liquids.remove(liquid.liquid, liquid.amount / plan.time * b.edelta())
                    }
                }
            }
        })
        consumePowerCond<HoloPBuild>(powerUse) {
            it.curPlan != null
        }
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
        UndebugOnly {
            removeItemsInBar()
        }
        DebugOnly {
            AddBar<HoloPBuild>(R.Bar.ProgressN,
                { R.Bar.Progress.bundle(progress.percentI) },
                { Pal.bar },
                { progress }
            )
        }.Else {
            AddBar<HoloPBuild>(R.Bar.Vanilla.BuildProgressN,
                { R.Bar.Vanilla.BuildProgress.bundle },
                { Pal.bar },
                { progress }
            )
        }
        AddBar<HoloPBuild>(R.Bar.Vanilla.UnitsN,
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

    protected val Int.plan: HoloPlan?
        get() = if (this < 0 || this >= plans.size)
            null
        else
            plans[this]

    open inner class HoloPBuild : Building() {
        @Serialized
        var planOrder: Int = -1
        val curPlan: HoloPlan?
            get() = planOrder.plan
        @Serialized
        var progressTime = 0f
        var commandPos: Vec2? = null
        override fun block(): HoloProjector = this@HoloProjector
        val progress: Float
            get() {
                val plan = curPlan
                return if (plan != null)
                    (progressTime / plan.time).coerceIn(0f, 1f)
                else
                    0f
            }

        override fun updateTile() {
            if (!canConsume()) return
            val plan = curPlan ?: return
            progressTime += edelta()

            if (progressTime >= plan.time) {
                val projected = projectUnit(plan.unitType)
                if (projected) {
                    consume()
                    progressTime = 0f
                }
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
            val p = curPlan
            progressTime = if (p != null)
                progressTime.coerceAtMost(p.time)
            else
                0f
        }

        override fun buildConfiguration(table: Table) {
            val options = Seq.with(plans).map {
                it.unitType
            }.filter {
                it.unlockedNow() && !it.isBanned
            }
            if (options.any()) {
                table.addItemSelectorDefault(this@HoloProjector, options,
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

        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            return true
        }

        @JvmField var lastUnitInPayload: MdtUnit? = null
        fun findTrueHoloProjectorSource(): HoloPBuild {
            val unit = lastUnitInPayload
            if (unit is HoloUnit) {
                val trueProjector = unit.projectorPos.TE<HoloPBuild>()
                if (trueProjector != null)
                    return trueProjector
            }
            return this
        }

        override fun updatePayload(unitHolder: Unit?, buildingHolder: Building?) {
            lastUnitInPayload = unitHolder
            super.updatePayload(unitHolder, buildingHolder)
        }

        override fun config(): Any? = planOrder
        open fun projectUnit(unitType: HoloUnitType): Boolean {
            if (unitType.canCreateHoloUnitIn(team)) {
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
                return true
            }
            return false
        }
        @ClientOnly
        var alpha = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        @ClientOnly
        var lastPlan: HoloPlan? = curPlan
        @ClientOnly
        var projecting = 0f
        override fun draw() {
            super.draw()
            val curPlan = curPlan
            val delta = if (canConsume() && curPlan != null)
                0.015f
            else
                -0.015f
            alpha += delta * Time.delta
            val planDraw = curPlan ?: lastPlan
            if (lastPlan != curPlan)
                lastPlan = curPlan
            if (alpha <= 0.01f) return
            if (planDraw != null) {
                SD.Hologram.use {
                    val type = planDraw.unitType
                    it.alpha = (progress * 1.2f * alpha).coerceAtMost(1f)
                    it.flickering = it.DefaultFlickering - (1f - progress) * 0.4f
                    if (type.ColorOpacity > 0f)
                        it.blendFormerColorOpacity = type.ColorOpacity
                    if (type.HoloOpacity > 0f) {
                        it.blendHoloColorOpacity = type.HoloOpacity
                    }
                    type.uiIcon.Draw(x, y)
                }
            }
            WhenNotPaused {
                if (progress < 1f && !inPayload) {
                    projecting += delta()
                }
            }
            val rotation = projecting
            val size = block.size * Vars.tilesize / projectorCenterRate
            // tx and ty control the position of bottom edge
            val tx = x
            val ty = y
            Lines.stroke(1.0f)
            Draw.color(S.HologramDark)
            Draw.alpha(alpha)
            // the floating of center
            val focusLen = 3.8f + Mathf.absin(projecting, 3.0f, 0.6f)
            val px = x + Angles.trnsx(rotation, focusLen)
            val py = y + Angles.trnsy(rotation, focusLen)
            val shrink = projectorShrink
            // the vertices
            vecs[0].set(tx - size, ty - size) // left-bottom
            vecs[1].set(tx + size, ty - size) // right-bottom
            vecs[2].set(tx - size, ty + size) // left-top
            vecs[3].set(tx + size, ty + size) // right-top
            Draw.z(Layer.buildBeam)
            if (Vars.renderer.animateShields) {
                Fill.tri(px, py, vecs[0].x + shrink, vecs[0].y, vecs[1].x - shrink, vecs[1].y) // bottom
                Fill.tri(px, py, vecs[2].x + shrink, vecs[2].y, vecs[3].x - shrink, vecs[3].y) // up
                Fill.tri(px, py, vecs[0].x, vecs[0].y + shrink, vecs[2].x, vecs[2].y - shrink) // left
                Fill.tri(px, py, vecs[1].x, vecs[1].y + shrink, vecs[3].x, vecs[3].y - shrink) // right
            } else {
                // bottom
                Lines.line(px, py, vecs[0].x + shrink, vecs[0].y)
                Lines.line(px, py, vecs[1].x - shrink, vecs[1].y)
                // up
                Lines.line(px, py, vecs[2].x + shrink, vecs[2].y)
                Lines.line(px, py, vecs[3].x - shrink, vecs[3].y)
                // left
                Lines.line(px, py, vecs[0].x, vecs[0].y + shrink)
                Lines.line(px, py, vecs[2].x, vecs[3].y - shrink)
                // right
                Lines.line(px, py, vecs[1].x, vecs[1].y + shrink)
                Lines.line(px, py, vecs[3].x, vecs[3].y - shrink)
            }
            Draw.reset()
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
            team.updateHoloCapacity(this)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            planOrder = read.b().toInt()
            progressTime = read.f()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.b(planOrder)
            write.f(progressTime)
        }

        override fun senseObject(sensor: LAccess): Any? {
            return when (sensor) {
                LAccess.config -> planOrder
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
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.itemCapacity)

        stats.add(Stat.output) { stat ->
            val p: Seq<HoloPlan> = plans.select { plan ->
                plan.unitType.unlockedNow()
            }
            stat.row()
            for (plan in p) {
                val type = plan.unitType
                stat.image(type.uiIcon).size((8 * 3).toFloat()).padRight(2f).right()
                stat.add(type.localizedName)
                    .color(cyberion.color).left()
                stat.table {
                    it.add("${autoFixed(plan.time / 60f, 1)} ${R.Bundle.CostSecond.bundle}")
                        .color(Pal.stat).padLeft(12f).left()
                    it.add(autoFixed(plan.req.cyberionReq, 1))
                        .color(cyberion.color).padLeft(12f).left()
                    it.image(cyberion.uiIcon).size((8 * 3).toFloat())
                        .padRight(2f).right()
                }
                stat.row()
            }
        }
    }
}