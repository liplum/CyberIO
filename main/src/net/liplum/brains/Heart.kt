package net.liplum.brains

import arc.audio.Sound
import arc.graphics.Color
import arc.math.Mathf
import arc.scene.ui.layout.Table
import arc.util.Time
import arc.util.Tmp
import mindustry.Vars
import mindustry.content.Bullets
import mindustry.content.UnitTypes
import mindustry.entities.Effect
import mindustry.entities.Mover
import mindustry.entities.UnitSorts
import mindustry.entities.Units
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
import net.liplum.*
import net.liplum.CioMod.Companion.DebugMode
import net.liplum.api.brain.*
import net.liplum.lib.Draw
import net.liplum.lib.animations.anims.Anime
import net.liplum.lib.animations.anims.linearFrames
import net.liplum.lib.animations.anims.randomCurTime
import net.liplum.lib.bundle
import net.liplum.lib.mixin.Mover
import net.liplum.lib.ui.bars.AddBar
import net.liplum.lib.ui.bars.appendDisplayLiquidsDynamic
import net.liplum.lib.ui.bars.genAllLiquidBars
import net.liplum.lib.ui.bars.removeLiquidInBar
import net.liplum.utils.*

class Heart(name: String) : Block(name), IComponentBlock {
    // Upgrade component
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()
    @JvmField var bulletType: BulletType = Bullets.placeholder
    @JvmField var heartbeat = Heartbeat()
    @JvmField var shootPatternInit: ShootPattern.() -> Unit = {}
    // Blood
    @JvmField var blood: Blood = Blood.X
    @JvmField var downApproachSpeed = 0.00025f
    @JvmField var upApproachSpeed = 0.00001f
    // Improved by Heimdall
    @JvmField var bloodCapacity = 1000f
    @JvmField var bloodCapacityI = 0.5f
    @JvmField var temperatureI = 0.1f
    @JvmField var convertSpeed = 1f
    @JvmField var convertSpeedI = 0.5f
    // Turret
    @JvmField var soundPitchMin = 0.9f
    @JvmField var soundPitchMax = 1.1f
    // Visual effects
    @ClientOnly @JvmField var bloodColor: Color = R.C.Blood
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var HeartTR: TR
    @ClientOnly lateinit var HeartBeatTRs: TRs
    @ClientOnly @JvmField var HeartbeatDuration = 60f
    @ClientOnly @JvmField var HeartbeatFrameNum = 20
    @ClientOnly lateinit var allLiquidBars: Array<(Building) -> Bar>
    // Timer
    @JvmField var convertTimer = timers++

    init {
        solid = true
        update = true
        hasPower = true
        sync = true
        hasLiquids = true
        canOverdrive = false
    }

    override fun init() {
        checkInit()
        liquidCapacity = bloodCapacity * (1f + bloodCapacityI)
        consumePowerDynamic<HeartBuild> {
            it.realPowerUse
        }
        super.init()
        allLiquidBars = genAllLiquidBars()
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        HeartTR = this.sub("heart")
        HeartBeatTRs = this.sheet("beat", HeartbeatFrameNum)
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
        Drawf.dashCircle(
            x * Vars.tilesize + offset,
            y * Vars.tilesize + offset,
            heartbeat.range.base,
            bloodColor
        )
    }

    override fun setBars() {
        super.setBars()
        removeLiquidInBar()
        DebugOnly {
            addBrainInfo<HeartBuild>()
        }
        AddBar<HeartBuild>(R.Bar.BloodN, if (DebugMode) {
            { "${R.Bar.Blood.bundle}: ${bloodAmount.toInt()}" }
        } else {
            { R.Bar.Blood.bundle }
        }, {
            bloodColor
        }, {
            bloodAmount / realBloodCapacity
        })
        AddBar<HeartBuild>(R.Bar.TemperatureN, if (DebugMode) {
            { "${R.Bar.Temperature.bundle}: ${temperature.format(2)}" }
        } else {
            { R.Bar.Temperature.bundle }
        }, {
            Pal.power
        }, {
            temperature / 1f
        })
    }

    open inner class HeartBuild : Building(),
        IUpgradeComponent, ControlBlock, HeatBlock, Ranged {
        //<editor-fold desc="Heimdall">
        override var directionInfo: Direction2 = Direction2()
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade>
            get() = this@Heart.upgrades
        //</editor-fold>
        //<editor-fold desc="Controllable">
        var unit = UnitTypes.block.create(team) as BlockUnitc
        //</editor-fold>
        //<editor-fold desc="Heat Block">
        var heat = 0f
        //</editor-fold>
        //<editor-fold desc="Serialized">
        // Serialized
        @Serialized
        var temperature = blood.temperature
            set(value) {
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
        //</editor-fold>
        //<editor-fold desc="Meta">
        val curBulletType: BulletType
            get() = bulletType
        val curShooSound: Sound
            get() = heartbeat.sounds[heartbeat.soundIndexer(temperatureEfficiency)]
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
        var lastHeartBeatTime = 0f
        @ClientOnly
        lateinit var heartbeatAnime: Anime
        //</editor-fold>
        init {
            ClientOnly {
                heartbeatAnime = Anime(
                    HeartBeatTRs.linearFrames(HeartbeatDuration)
                ).apply {
                    isEnd = true
                    onEnd = {
                        if (lastHeartBeatTime < 10f) {
                            isEnd = false
                            index = 0
                        }
                    }
                    randomCurTime()
                }
            }
        }

        override fun updateTile() {
            reloadCounter += edelta()
            logicControlTime -= Time.delta
            lastHeartBeatTime += Time.delta
            temperature = Mathf.approach(
                temperature, targetTemperature,
                temperatureChangeSpeed * Time.delta
            )
            if (efficiency <= 0f) return
            if (timer(convertTimer, 1f)) {
                convertBlood()
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
                    consumeBlood()
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
            val speed = realBloodConvertSpeed * efficiency
            for (liquid in Vars.content.liquids()) {
                var amount = liquids[liquid]
                if (amount > 0f) {
                    amount = amount.coerceAtMost(speed)
                    // Calculate enthalpy
                    val H = liquid.calcuEnthalpy(amount)
                    bloodAmount += amount
                    if (liquid.temperature < temperature) {
                        temperature -= H / blood.heatCapacity / bloodAmount
                    } else {
                        temperature += H / blood.heatCapacity / bloodAmount
                    }
                    liquids.remove(liquid, amount)
                    if (bloodAmount >= capacity) return
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

        override fun draw() {
            WhenNotPaused {
                val speedUp = temperatureEfficiency + 1f
                heartbeatAnime.spend(
                    (Time.delta * speedUp) * heartbeatFactor
                )
            }
            BaseTR.Draw(x, y)
            heartbeatAnime.draw(x, y)
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
        }

        open fun consumeBlood() {
            bloodAmount -= realBloodCost
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
            curShooSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax))

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

        override fun unit(): MdtUnit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as MdtUnit)
        }

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

        override fun displayBars(table: Table) {
            this.appendDisplayLiquidsDynamic(
                table, allLiquidBars
            ) {
                super.displayBars(table)
            }
        }
        // Implement heat
        override fun heat() = heat
        override fun heatFrac() = heat / 5f
        override fun range() = realRange
        override fun drawSelect() {
            Drawf.dashCircle(x, y, realRange, bloodColor)
        }
    }
}