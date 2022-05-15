package net.liplum.brains

import arc.audio.Sound
import arc.graphics.Color
import arc.math.Mathf
import arc.util.Time
import mindustry.Vars
import mindustry.content.Fx
import mindustry.content.UnitTypes
import mindustry.entities.Effect
import mindustry.entities.Mover
import mindustry.entities.UnitSorts
import mindustry.entities.Units
import mindustry.entities.bullet.BulletType
import mindustry.entities.pattern.ShootPattern
import mindustry.gen.*
import mindustry.graphics.Drawf
import mindustry.logic.LAccess
import mindustry.logic.Ranged
import mindustry.type.Liquid
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import mindustry.world.blocks.heat.HeatBlock
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.Serialized
import net.liplum.WhenNotPaused
import net.liplum.api.brain.*
import net.liplum.lib.Draw
import net.liplum.lib.animations.anims.Anime
import net.liplum.lib.animations.anims.linearFrames
import net.liplum.lib.animations.anims.randomCurTime
import net.liplum.utils.*

class Heart(name: String) : Block(name), IComponentBlock {
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()
    // Normal
    lateinit var normalBullet: BulletType
    @JvmField var normalHeartBeat: Sound = Sounds.none
    @JvmField var normalPattern = HeartBeatShootPattern.X
    @JvmField var normalSound: Sound = Sounds.none
    @JvmField var normalShake = 0f
    // Improved
    lateinit var improvedBullet: BulletType
    @JvmField var improvedHeartBeat: Sound = Sounds.none
    @JvmField var improvedPattern = HeartBeatShootPattern.X
    @JvmField var improvedSound: Sound = Sounds.none
    @JvmField var improvedShake = 0f
    // Temperature
    @JvmField var coreHeat = 5f
    @JvmField var bloodCost = 1f
    @JvmField var bloodCostI = 1f
    @JvmField var reloadTime = 60f
    @JvmField var reloadTimeI = -0.2f
    @JvmField var range = 165f
    @JvmField var rangeI = 0.45f
    @JvmField var maxHeartbeatSpeedUp = 1.5f
    // Turret
    @JvmField var soundPitchMin = 0.9f
    @JvmField var soundPitchMax = 1.1f
    @JvmField var shootEffect: Effect = Fx.none
    @JvmField var chargeSound: Sound = Sounds.none
    @ClientOnly @JvmField var bloodColor: Color = R.C.Blood
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var HeartTR: TR
    @ClientOnly lateinit var HeartBeatTRs: TRs
    @ClientOnly @JvmField var HeartbeatDuration = 60f
    @ClientOnly @JvmField var HeartbeatFrameNum = 20

    init {
        solid = true
        update = true
        hasPower = true
        sync = true
        canOverdrive = false
    }

    override fun init() {
        checkInit()
        super.init()
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
        Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset, range, bloodColor)
    }

    open inner class HeartBuild : Building(),
        IUpgradeComponent, ControlBlock, HeatBlock, Ranged {
        override var directionInfo: Direction2 = Direction2()
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade>
            get() = this@Heart.upgrades
        var unit = UnitTypes.block.create(team) as BlockUnitc
        var heat = 0f
        /**
         *  Q = mcΔT
         */
        @Serialized
        var temperature = 0f
        @Serialized
        var bloodAmount = Float.MAX_VALUE
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @Serialized
        var reloadCounter = 0f
        var logicControlTime: Float = -1f
        val logicControlled: Boolean
            get() = logicControlTime > 0
        var logicShoot = false
        val realBloodCost: Float
            get() = bloodCost * (1f + if (isLinkedBrain) bloodCostI else 0f)
        val curShootPattern: ShootPattern
            get() = if (isLinkedBrain) improvedPattern else normalPattern
        val curBulletType: BulletType
            get() = if (isLinkedBrain) improvedBullet else normalBullet
        val hasEnoughBlood: Boolean
            get() = bloodAmount >= realBloodCost
        val curShootEffect: Effect
            get() = shootEffect
        val curShooSound: Sound
            get() = if (isLinkedBrain) improvedSound else normalSound
        val curShake: Float
            get() = if (isLinkedBrain) improvedShake else normalShake
        val realReloadTime: Float
            get() = reloadTime * (1f + if (isLinkedBrain) reloadTimeI else 0f)
        val realRange: Float
            get() = range * (1f + if (isLinkedBrain) rangeI else 0f)
        val sideHeat = FloatArray(4)
        @ClientOnly
        var heartbeatFactor = 1f
        @ClientOnly
        var lastHeartBeatTime = 0f
        @ClientOnly
        lateinit var heartbeatAnime: Anime
        val heartbeatSpeedUp: Float
            get() = if(isLinkedBrain) maxHeartbeatSpeedUp else 0f

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

        open fun findTarget(nextBullet: BulletType? = null): Teamc? {
            var result = Units.bestTarget(
                team, x, y, realRange,
                { !it.dead() },
                { true },
                UnitSorts.weakest
            )
            if (result == null && nextBullet != null && nextBullet.heals()) {
                result = Units.findAllyTile(team, x, y, range) {
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
                heartbeatAnime.spend(
                    (Time.delta + Mathf.random()) * heartbeatFactor + heartbeatSpeedUp
                )
            }
            BaseTR.Draw(x, y)
            heartbeatAnime.draw(x, y)
        }
        /**
         * Heart doesn't allow gas
         */
        override fun acceptLiquid(source: Building, liquid: Liquid): Boolean {
            if (liquid.gas) return false
            return super.acceptLiquid(source, liquid)
        }

        var queuedBullets = 0
        var totalShots = 0
        open fun shoot(type: BulletType) {
            val shoot = curShootPattern
            val bulletX = x
            val bulletY = y
            if (shoot.firstShotDelay > 0) {
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax))
                type.chargeEffect.at(bulletX, bulletY)
            }
            shoot.shoot(totalShots) { xOffset, yOffset, angle, delay, mover ->
                queuedBullets++
                if (delay > 0f) {
                    Time.run(delay) { bullet(type, xOffset, yOffset, angle, mover) }
                } else {
                    bullet(type, xOffset, yOffset, angle, mover)
                }
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
            curShootEffect.at(bulletX, bulletY, angleOffset, type.hitColor)
            curShooSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax))

            if (curShake > 0) {
                Effect.shake(curShake, curShake, this)
            }
            // No turret heat
            // heat = 1f
        }

        open fun handleBullet(bullet: Bullet?, offsetX: Float, offsetY: Float, angleOffset: Float) {
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

        override fun heat() = heat
        override fun heatFrac() = heat / coreHeat
        override fun range() = realRange
        override fun drawSelect() {
            Drawf.dashCircle(x, y, realRange, bloodColor)
        }
    }
}