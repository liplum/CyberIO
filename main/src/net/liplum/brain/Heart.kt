package net.liplum.brain

import arc.audio.Sound
import arc.func.Prov
import arc.graphics.Color
import arc.math.Mathf
import arc.math.Mathf.pow
import arc.scene.ui.layout.Table
import arc.struct.EnumSet
import arc.util.Time
import arc.util.Tmp
import mindustry.Vars
import mindustry.content.Bullets
import mindustry.content.UnitTypes
import mindustry.entities.*
import mindustry.entities.bullet.BulletType
import mindustry.entities.pattern.ShootPattern
import mindustry.gen.*
import mindustry.graphics.Drawf
import mindustry.graphics.Pal
import mindustry.logic.LAccess
import mindustry.logic.Ranged
import mindustry.type.Liquid
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import mindustry.world.blocks.heat.HeatBlock
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.api.brain.*
import net.liplum.common.Smooth
import net.liplum.common.util.format
import net.liplum.common.util.toDouble
import net.liplum.input.smoothPlacing
import net.liplum.input.smoothSelect
import net.liplum.render.G
import net.liplum.render.HeatMeta
import net.liplum.render.drawHeat
import net.liplum.ui.bars.appendDisplayLiquidsDynamic
import net.liplum.ui.bars.genAllLiquidBars
import net.liplum.ui.bars.removeLiquidInBar
import net.liplum.utils.WhenTheSameTeam
import net.liplum.utils.sheet
import net.liplum.utils.sub
import plumy.animation.*
import plumy.animation.ContextDraw.Draw
import plumy.animation.ContextDraw.DrawScale
import plumy.core.*
import plumy.core.arc.hsvLerp
import plumy.core.assets.TR
import plumy.core.assets.TRs
import plumy.core.math.FUNC
import plumy.core.math.isZero
import plumy.dsl.AddBar
import plumy.dsl.Mover
import plumy.dsl.bundle

open class Heart(name: String) : Block(name), IComponentBlock {
    // Upgrade component
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()
    @JvmField
    var bulletType: BulletType = Bullets.placeholder
    @JvmField
    var heartbeat = Heartbeat()
    @JvmField
    var shootPatternInit: ShootPattern.() -> Unit = {}
    // Blood
    @JvmField
    var blood: Blood = Blood.X
    @JvmField
    var downApproachSpeed = 0.00004f
    @JvmField
    var upApproachSpeed = 0.00002f
    @JvmField
    var heatFactor = 5f
    @JvmField
    var heatMax = 2.5f
    @JvmField
    var temperatureConvertFactor = 0.1f
    @JvmField
    var bloodConsumePreTick = 0.2f
    // Improved by Heimdall
    @JvmField
    var bloodCapacity = 1000f
    @JvmField
    var bloodCapacityI = 0.5f
    @JvmField
    var temperatureI = 0.1f
    @JvmField
    var convertSpeed = 1f
    @JvmField
    var convertSpeedI = 0.5f
    // Turret
    @JvmField
    var soundPitchMin = 0.9f
    @JvmField
    var soundPitchMax = 1.1f
    // Visual effects
    @ClientOnly
    @JvmField
    var bloodColor: Color = R.C.Blood
    @ClientOnly
    @JvmField
    var coldColor: Color = R.C.ColdTemperature
    @ClientOnly
    @JvmField
    var hotColor: Color = R.C.HotTemperature
    @ClientOnly
    @JvmField
    var maxSelectedCircleTime = Var.SelectedCircleTime
    @ClientOnly
    lateinit var BaseTR: TR
    @ClientOnly
    lateinit var HeartTR: TR
    @ClientOnly
    lateinit var HeartBeatTRs: TRs
    @ClientOnly
    lateinit var HeatTRs: TRs
    @ClientOnly
    @JvmField
    var HeartbeatDuration = 60f
    @ClientOnly
    @JvmField
    var HeartbeatFrameNum = 20
    /** The Higher, the weaker scale*/
    @ClientOnly
    @JvmField
    var BreathIntensity = 40f
    @ClientOnly
    lateinit var allLiquidBars: Array<(Building) -> Bar>
    @ClientOnly
    @JvmField
    val heatMeta = HeatMeta()
    @JvmField
    var TemperatureEFF2ASpeed: FUNC = {
        if (it >= 0f) it + 1
        else pow(4f, it)
    }
    // Timer
    @JvmField
    var convertOrConsumeTimer = timers++

    init {
        buildType = Prov { HeartBuild() }
        solid = true
        update = true
        hasPower = true
        sync = true
        updateInUnits = true
        alwaysUpdateInUnits = true
        attacks = true
        hasLiquids = true
        canOverdrive = false
        priority = TargetPriority.turret
        group = BlockGroup.turrets
        flags = EnumSet.of(BlockFlag.turret)
        conductivePower = true
    }

    override fun init() {
        checkInit()
        liquidCapacity = bloodCapacity * (1f + bloodCapacityI)
        consumePowerDynamic<HeartBuild> {
            it.realPowerUse
        }
        heatMax = (1f - blood.temperature) * heatFactor
        super.init()
        allLiquidBars = genAllLiquidBars()
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        HeartTR = this.sub("heart")
        HeartBeatTRs = this.sheet("beat", HeartbeatFrameNum)
        HeatTRs = this.sheet("heat", HeartbeatFrameNum)
    }

    override fun setStats() {
        super.setStats()
        this.addUpgradeComponentStats()
    }

    override fun icons() = arrayOf(
        BaseTR, HeartTR
    )

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        G.dashCircleBreath(this, x, y, heartbeat.range.base * smoothPlacing(maxSelectedCircleTime), bloodColor)
    }

    protected val temperatureColor = Color()
    override fun setBars() {
        super.setBars()
        removeLiquidInBar()
        addBrainLinkInfo<HeartBuild>()
        DebugOnly {
            AddBar<HeartBuild>(R.Bar.BloodN,
                { "${R.Bar.Blood.bundle}: ${bloodAmount.toInt()}" },
                { bloodColor },
                { bloodAmount / realBloodCapacity })
        }.Else {
            AddBar<HeartBuild>(R.Bar.BloodN,
                { R.Bar.Blood.bundle },
                { bloodColor },
                { bloodAmount / realBloodCapacity })
        }

        DebugOnly {
            AddBar<HeartBuild>(R.Bar.TemperatureN,
                { "${R.Bar.Temperature.bundle}: ${temperature.format(2)}" },
                { temperatureColor.set(coldColor).hsvLerp(hotColor, temperature) },
                { temperature })
        }.Else {
            AddBar<HeartBuild>(R.Bar.TemperatureN,
                { R.Bar.Temperature.bundle },
                { temperatureColor.set(coldColor).hsvLerp(hotColor, temperature) },
                { temperature })
        }
        DebugOnly {
            AddBar<HeartBuild>("reload",
                { "Reload: ${reloadCounter.toInt()}/ ${realReloadTime.toInt()}" }, {
                    Pal.power
                }, {
                    reloadCounter / realReloadTime
                }
            )
            AddBar<HeartBuild>("speed",
                { "Speed: ${TemperatureEFF2ASpeed(temperatureEfficiency).format(2)}" }, {
                    Pal.power
                }, {
                    TemperatureEFF2ASpeed(temperatureEfficiency) / 2f
                }
            )
        }
        AddBar<HeartBuild>("heat",
            { "bar.heat".bundle },
            { Pal.power },
            { heat / heatMax }
        )
    }

    open inner class HeartBuild : Building(),
        IUpgradeComponent, ControlBlock, HeatBlock, Ranged {
        //<editor-fold desc="Heimdall">
        override val componentName = "Heart"
        override val scale: SpeedScale = SpeedScale()
        override var directionInfo: Direction2 = Direction2()
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade>
            get() = this@Heart.upgrades
        /**
         * Do not use it.
         */
        override var heatShared = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }

        override fun afterPickedUp() {
            super.afterPickedUp()
            unlinkBrain()
        }
        //</editor-fold>
        //<editor-fold desc="Controllable">
        var unit = UnitTypes.block.create(team) as BlockUnitc
        override fun unit(): MUnit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as MUnit)
        }
        //</editor-fold>
        //<editor-fold desc="Heat Block">
        val heat: Float
            get() = ((temperature - blood.temperature) * heatFactor).coerceAtLeast(0f)
        //</editor-fold>
        //<editor-fold desc="Serialized">
        // Serialized
        @Serialized
        var temperature = blood.temperature
            set(value) {
                // Prevent inputting more scorching liquid but can't handle its value
                field = value.coerceIn(0f, 1f)
            }
        @Serialized
        var bloodAmount = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @Serialized
        var reloadCounter = 0f
        //</editor-fold">
        //<editor-fold desc="Logic">
        var logicControlTime: Float = -1f
        val logicControlled: Boolean
            get() = logicControlTime > 0
        var logicShoot = false
        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            if (type == LAccess.shoot && !unit.isPlayer) {
                logicControlTime = 60f
                logicShoot = !p3.isZero
            }
            super.control(type, p1, p2, p3, p4)
        }

        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            if (type == LAccess.shootp && !unit.isPlayer) {
                if (p1 is Posc) {
                    logicControlTime = 60f
                    logicShoot = !p2.isZero
                }
            }
            super.control(type, p1, p2, p3, p4)
        }

        override fun sense(sensor: LAccess): Double {
            return when (sensor) {
                LAccess.ammo -> (bloodAmount).toDouble()
                LAccess.ammoCapacity -> (realBloodCapacity).toDouble()
                LAccess.shooting -> (reloadCounter < realReloadTime).toDouble()
                LAccess.progress -> (reloadCounter / realReloadTime).toDouble()
                LAccess.heat -> heat().toDouble()
                else -> super.sense(sensor)
            }
        }
        //</editor-fold>
        //<editor-fold desc="Properties">
        val realReloadTime: Float
            get() = heartbeat.reloadTime.progress(temperatureEfficiency)
        val realPowerUse: Float
            get() = heartbeat.powerUse.progress(temperatureEfficiency)
        val realBloodCost: Float
            get() = heartbeat.bloodCost.progress(temperatureEfficiency)
        val realRange: Float
            get() = heartbeat.range.progress(temperatureEfficiency)
        val realShake: Float
            get() = heartbeat.shake.progress(temperatureEfficiency)
        val realShootNumber: Int
            get() = heartbeat.shootNumber.progress(temperatureEfficiency)
        val realSystole: Float
            get() = heartbeat.systole.progress(temperatureEfficiency)
        val realDiastole: Float
            get() = heartbeat.diastole.progress(temperatureEfficiency)
        val realDamage: Float
            get() = heartbeat.damage.progress(temperatureEfficiency)
        val realBulletLifeTime: Float
            get() = heartbeat.bulletLifeTime.progress(temperatureEfficiency)
        val realBloodConsumePreTick: Float
            get() = bloodConsumePreTick * (2f - temperature) * 1.5f
        @ClientOnly
        val realBreathIntensity: Float
            get() = BreathIntensity - if (isLinkedBrain) 10f else 0f
        //</editor-fold>
        //<editor-fold desc="Meta">
        val curBulletType: BulletType
            get() = bulletType
        val curShooSound: Sound
            get() = heartbeat.soundGetter(temperatureEfficiency)
        val shootPattern = object : ShootPattern() {
            val systoleMover = Mover {
                if (timer.get(5, heartbeat.systoleTime)) {
                    vel.scl(1f - realSystole)
                }
                if (vel.len() < heartbeat.systoleMinIn) {
                    vel.setLength(realDiastole)
                }
            }

            override fun shoot(totalShots: Int, handler: BulletHandler) {
                val shots = realShootNumber
                val perAngle = 360f / shots
                val offset = Tmp.v1.set(heartbeat.offset, 0f)
                for (i in 0 until shots) {
                    val angle = perAngle * i
                    offset.setAngle(angle)
                    handler.shoot(
                        0f + offset.x,
                        0f + offset.y,
                        angle,
                        firstShotDelay + shotDelay * i,
                        systoleMover
                    )
                }
            }
        }.apply(shootPatternInit)
        //</editor-fold>
        //<editor-fold desc="Improved by Heimdall">
        val realBloodCapacity: Float
            get() = bloodCapacity * (1f + if (isLinkedBrain) bloodCapacityI else 0f)
        val realBloodConvertSpeed: Float
            get() = if (isLinkedBrain) convertSpeed * (1f + convertSpeedI) else convertSpeed
        val targetTemperature: Float
            get() = if (isLinkedBrain) blood.temperature * (1f + temperatureI) else blood.temperature
        //</editor-fold>
        // Function
        //  0.0 -> -100%, 0.5f C -> 0%,, 1.0 C-> 100%
        val temperatureEfficiency: Float
            get() = (temperature / blood.temperature) - 1f
        val hasEnoughBlood: Boolean
            get() = bloodAmount >= realBloodCost
        // TODO: take Attribute.heat into account
        val temperatureChangeSpeed: Float
            get() = if (temperature > targetTemperature) downApproachSpeed else upApproachSpeed
        val temperatureDelta: Float
            get() = temperature - targetTemperature
        //<editor-fold desc="Visual effects">
        @ClientOnly
        var heartbeatFactor = 1f
        @ClientOnly
        var lastHeartBeatTime = 10f
        @ClientOnly
        lateinit var heartbeatAnime: Anime
        @ClientOnly
        var visualRange = Smooth(heartbeat.range.base).target {
            heartbeat.range.progress(temperatureEfficiency)
        }.speed(0.5f)
        //</editor-fold>
        init {
            ClientOnly {
                heartbeatAnime = Anime(
                    HeartBeatTRs.linearFrames(HeartbeatDuration)
                ).apply {
                    onEnd = {
                        if (lastHeartBeatTime < 10f) {
                            isEnd = false
                            index = 0
                        }
                    }
                    randomCurTime()
                    setEnd()
                }
            }
        }

        override fun updateTile() {
            scale.update()
            reloadCounter += edelta()
            logicControlTime -= Time.delta
            lastHeartBeatTime += Time.delta
            temperature = Mathf.approach(
                temperature, targetTemperature,
                temperatureChangeSpeed * delta()
            )
            val canConvertOrConsume = timer(convertOrConsumeTimer, 1f)
            if (canConvertOrConsume) {
                consumeBloodAsEnergy()
            }

            if (efficiency <= 0f) return
            if (isLinkedBrain) {
                onOtherParts {
                    heatShared = this@HeartBuild.heatFrac()
                    // TODO: When connect with brain, restore health by blood.
                }
            }
            if (canConvertOrConsume && bloodAmount < realBloodCapacity) {
                convertBlood()
                bloodAmount = bloodAmount.coerceAtMost(realBloodCapacity)
            }
            if (hasEnoughBlood && reloadCounter >= realReloadTime) {
                val nextBullet = retrieveNextBullet()
                val shot = if (isControlled) {
                    unit().isShooting
                } else if (logicControlled) {
                    logicShoot
                } else {
                    findTarget(nextBullet) != null
                }
                if (shot) {
                    shoot(nextBullet)
                    reloadCounter = 0f
                    consumeBloodAsBullet()
                    lastHeartBeatTime = 0f
                }
            }
        }
        /**
         *  Q = m C ΔT
         *  enthalpy = mass * capacity * temperature
         *  H = m C T
         *  Only consider another liquid mixing with blood
         */
        open fun convertBlood() {
            val capacity = realBloodCapacity
            if (bloodAmount >= capacity) return
            val speed = realBloodConvertSpeed * efficiency * scale.value
            for (liquid in Vars.content.liquids()) {
                if (bloodAmount >= capacity) return
                var amount = liquids[liquid]
                if (amount > 0f) {
                    amount = amount.coerceAtMost(speed).coerceAtMost(capacity - bloodAmount)
                    // Calculate enthalpy
                    val H = liquid.calcEnthalpy(amount)
                    bloodAmount += amount
                    val delta = (H / blood.heatCapacity / bloodAmount) * temperatureConvertFactor
                    if (liquid.temperature < temperature) {
                        temperature -= delta
                    } else {
                        temperature += delta
                    }
                    liquids.remove(liquid, amount)
                }
            }
        }

        open fun findTarget(nextBullet: BulletType? = null): Teamc? {
            var result = Units.bestTarget(
                team, x, y, realRange,
                { !it.dead() },
                { true },
                UnitSorts.weakest
            )
            if (result == null && nextBullet != null && nextBullet.heals()) {
                result = Units.findAllyTile(team, x, y, realRange) {
                    it.damaged() && it == this
                }
            }
            return result
        }

        open fun retrieveNextBullet(): BulletType {
            // This function can decide which the final bullet to be used.
            return curBulletType
        }

        override fun delta(): Float {
            return this.timeScale * Time.delta * speedScale
        }

        override fun draw() {
            WhenNotPaused {
                val speed = TemperatureEFF2ASpeed(temperatureEfficiency + 1f)
                heartbeatAnime.spend(
                    (speed * delta()) * heartbeatFactor
                )
                visualRange.update(delta())
            }
            BaseTR.Draw(x, y)
            //heartbeatAnime.draw(x, y)
            heartbeatAnime.draw {
                it.DrawScale(x, y, 1f + G.sin / realBreathIntensity)
            }
            heatMeta.drawHeat(this, HeatTRs[heartbeatAnime.index])
        }
        /**
         * Heart doesn't allow gas
         */
        override fun acceptLiquid(source: Building, liquid: Liquid): Boolean {
            return !liquid.gas && liquids[liquid] < liquidCapacity
        }

        var queuedBullets = 0
        var totalShots = 0
        open fun shoot(type: BulletType) {
            val shoot = shootPattern
            shoot.shoot(totalShots) { xOffset, yOffset, angle, _, mover ->
                queuedBullets++
                bullet(type, xOffset, yOffset, angle, mover)
                totalShots++
            }
            curShooSound.at(x, y, Mathf.random(soundPitchMin, soundPitchMax))
        }

        open fun consumeBloodAsBullet() {
            bloodAmount -= realBloodCost
        }

        open fun consumeBloodAsEnergy() {
            bloodAmount -= realBloodConsumePreTick
        }

        open fun bullet(type: BulletType, xOffset: Float, yOffset: Float, angleOffset: Float, mover: Mover?) {
            queuedBullets--
            if (dead) return
            val bulletX = x + xOffset
            val bulletY = y + yOffset
            val shootAngle = 0f + angleOffset
            handleBullet(
                type.create(
                    this, team, bulletX, bulletY, shootAngle, -1f, 1f, 1f, null, mover, x, y
                ), xOffset, yOffset, shootAngle
            )

            if (realShake > 0) {
                Effect.shake(realShake, realShake, this)
            }
        }

        open fun handleBullet(bullet: Bullet?, offsetX: Float, offsetY: Float, angleOffset: Float) {
            if (bullet != null) {
                bullet.damage += realDamage
                bullet.lifetime += realBulletLifeTime
            }
        }

        override fun heal() {
            super.heal()
            trigger(Trigger.heal)
        }

        override fun heal(amount: Float) {
            super.heal(amount)
            trigger(Trigger.heal)
        }

        override fun remove() {
            super.remove()
            clear()
        }

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            trigger(Trigger.partDestroyed)
            clear()
        }

        override fun displayBars(table: Table) {
            super.displayBars(table)
            WhenTheSameTeam {
                this.appendDisplayLiquidsDynamic(
                    table, allLiquidBars
                ) {
                    super.displayBars(table)
                }
            }
        }
        // For output heat
        override fun heat() = heat * heatMax
        override fun heatFrac() = heat / heatMax
        override fun range() = realRange
        override fun drawSelect() {
            Drawf.dashCircle(x, y, visualRange.value * smoothSelect(maxSelectedCircleTime), bloodColor)
        }
    }
}