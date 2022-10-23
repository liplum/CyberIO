package net.liplum.brain

import arc.audio.Sound
import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Interp
import arc.math.Mathf
import arc.util.Time
import mindustry.Vars
import mindustry.content.Bullets
import mindustry.entities.TargetPriority
import mindustry.entities.bullet.BulletType
import mindustry.gen.*
import mindustry.gen.Unit
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.world.blocks.defense.turrets.PowerTurret
import mindustry.world.meta.Stat
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.api.brain.*
import net.liplum.common.math.PolarX
import net.liplum.common.util.progress
import net.liplum.input.smoothPlacing
import net.liplum.input.smoothSelect
import plumy.core.ClientOnly
import plumy.core.WhenNotPaused
import plumy.animation.Anime
import plumy.animation.ContextDraw.Draw
import plumy.animation.draw
import plumy.animation.genFramesBy
import plumy.animation.randomCurTime
import net.liplum.render.*
import net.liplum.ui.ammoStats
import net.liplum.utils.draw
import net.liplum.utils.sheet
import net.liplum.utils.sub
import net.liplum.render.G
import net.liplum.render.HeatMeta
import net.liplum.render.drawHeat
import plumy.core.assets.EmptySounds
import plumy.core.assets.TR
import plumy.core.assets.TRs
import plumy.core.math.approachA
import plumy.core.math.approachR
import plumy.core.math.radian
import plumy.core.math.random
import plumy.dsl.TileXY
import plumy.dsl.getCenterWorldXY

open class Eye(name: String) : PowerTurret(name), IComponentBlock {
    var normalBullet: BulletType = Bullets.placeholder
    var improvedBullet: BulletType = Bullets.placeholder
    @ClientOnly @JvmField var normalSounds: Array<Sound> = EmptySounds
    @ClientOnly @JvmField var normalSoundPitchRange = 0.8f..1.2f
    @ClientOnly @JvmField var improvedSounds: Array<Sound> = EmptySounds
    @ClientOnly @JvmField var improvedSoundPitchRange = 0.8f..1.2f
    @ClientOnly @JvmField var soundVolume = 1f
    /**
     * Cooling per tick. It should be multiplied by [Time.delta]
     */
    @JvmField var coolingSpeed = 0.01f
    // Visual Effects
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var BaseHeatTR: TR
    @ClientOnly lateinit var EyeBallTR: TR
    @ClientOnly lateinit var EyelidTR: TR
    @ClientOnly lateinit var PupilTR: TR
    @ClientOnly lateinit var PupilHeatTR: TR
    @ClientOnly lateinit var PupilOutsideTR: TR
    @ClientOnly lateinit var PupilOutsideHeatTR: TR
    @ClientOnly lateinit var HemorrhageTRs: TRs
    @ClientOnly lateinit var BlinkTRs: TRs
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime
    @ClientOnly @JvmField val heatMeta = HeatMeta()
    @ClientOnly @JvmField var BlinkDuration = 50f
    @ClientOnly @JvmField var BlinkFrameNum = 9
    @ClientOnly @JvmField var radiusSpeed = 0.1f
    @ClientOnly @JvmField var PupilMax = 4.3f
    @ClientOnly @JvmField var PupilMin = 1.2f
    @ClientOnly @JvmField var outOfCompactTime = 240f
    @ClientOnly @JvmField var continuousShootCheckTime = 10f
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()
    @ClientOnly @JvmField var maxHemorrhageShotsReq = 5
    // Timer
    @JvmField var conversationTimer = timers++

    init {
        buildType = Prov { EyeBuild() }
        priority = TargetPriority.turret
        canOverdrive = false
        conductivePower = true
        shootSound = Sounds.none
    }

    override fun init() {
        updateInUnits = true
        alwaysUpdateInUnits = true
        // To prevent accessing a null
        shootType = Bullets.placeholder
        checkInit()
        super.init()
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        BaseHeatTR = this.sub("heat")
        EyeBallTR = this.sub("eyeball")
        EyelidTR = this.sub("eyelid")
        BlinkTRs = this.sheet("blink", BlinkFrameNum)
        PupilTR = this.sub("pupil")
        PupilHeatTR = this.sub("pupil-heat")
        PupilOutsideTR = this.sub("pupil-outside")
        PupilOutsideHeatTR = this.sub("pupil-outside-heat")
        HemorrhageTRs = this.sheet("hemorrhage", 3)
    }

    override fun icons() = arrayOf(
        BaseTR, EyelidTR
    )

    override fun setBars() {
        super.setBars()
        addBrainLinkInfo<EyeBuild>()
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.ammo)
        stats.add(Stat.ammo, ammoStats(Pair(this, normalBullet), Pair(this, improvedBullet)))
        this.addUpgradeComponentStats()
    }

    override fun drawPlace(x: TileXY, y: TileXY, rotation: Int, valid: Boolean) {
        drawPotentialLinks(x, y)
        val worldX = getCenterWorldXY(x)
        val worldY = getCenterWorldXY(y)
        drawOverlay(worldX, worldY, rotation)
        G.dashCircleBreath(worldX, worldY, range * smoothPlacing(maxSelectedCircleTime), Pal.placing, stroke = Var.CircleStroke)
    }

    open inner class EyeBuild : PowerTurretBuild(), IUpgradeComponent {
        //<editor-fold desc="Heimdall">
        override val componentName = "Eye"
        override val scale: SpeedScale = SpeedScale()
        override var directionInfo: Direction2 = Direction2.Empty
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade>
            get() = this@Eye.upgrades
        override var heatShared = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        //</editor-fold>
        @ClientOnly
        lateinit var blinkAnime: Anime

        init {
            ClientOnly {
                blinkAnime = Anime(
                    BlinkTRs.genFramesBy(BlinkDuration) {
                        Interp.pow2In.apply(it)
                    }
                ).apply {
                    var forward = true
                    isForward = {
                        forward || isShooting || charging()
                    }
                    onEnd = {
                        if (!isShooting && !charging() && !pupilIsApproachingMin) {
                            forward = !forward
                            isEnd = false
                        }
                    }
                    randomCurTime()
                }
            }
        }

        override fun afterPickedUp() {
            super.afterPickedUp()
            unlinkBrain()
        }

        override fun delta(): Float {
            return this.timeScale * Time.delta * speedScale * (1f + heatShared)
        }

        override fun updateTile() {
            val conversationOn = timer(conversationTimer, 1f)
            scale.update()
            heatShared -= coolingSpeed * Time.delta
            super.updateTile()
            val target = target
            if (conversationOn &&
                target != null && target is Teamc &&
                target.team() != this.team
            ) {
                if (Mathf.chance(0.005)) {
                    trigger(Trigger.eyeDetect)
                }
            }
            ClientOnly {
                lastPayloadHolder = null
            }
        }

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            trigger(Trigger.partDestroyed)
            clear()
        }
        @ClientOnly
        var lastPayloadHolder: Unit? = null
        override fun updatePayload(unitHolder: Unit?, buildingHolder: Building?) {
            super.updatePayload(unitHolder, buildingHolder)
            ClientOnly {
                lastPayloadHolder = unitHolder
            }
        }

        override fun remove() {
            super.remove()
            clear()
        }
        @ClientOnly
        val stareAtScreenRadius: Float
            get() = size * Vars.tilesize * 2f
        @ClientOnly
        val sight = PolarX(0f, 0f)
        @ClientOnly
        var lastInCombatTime = outOfCompactTime
        @ClientOnly
        val isOutOfCombat: Boolean
            get() = lastInCombatTime >= outOfCompactTime
        val eyeColor: Color = R.C.RedAlert
        @ClientOnly
        var blinkFactor = 1f
        @ClientOnly
        var pupilIsApproachingMin = false
        @ClientOnly
        var continuousShots = 0
        fun checkContinuousShooting() =
            lastInCombatTime < reload + continuousShootCheckTime + shoot.firstShotDelay

        override fun draw() {
            WhenNotPaused {
                blinkAnime.spend(((Mathf.random() * 1) + Time.delta) * blinkFactor)
                lastInCombatTime += Time.delta
                if (!checkContinuousShooting())
                    continuousShots = 0
            }
            BaseTR.Draw(x, y)
            heatMeta.drawHeat(this, BaseHeatTR, heatShared)
            Draw.color()

            Draw.z(Layer.turret)
            val rotationDraw = rotation.draw
            Drawf.shadow(EyeBallTR, x - elevation, y - elevation)
            EyeBallTR.Draw(x, y)
            val radiusSpeed = radiusSpeed * Time.delta
            val consValid = canConsume()

            WhenNotPaused {
                if (consValid && (isShooting || lastInCombatTime < 40f || charging())) {
                    sight.approachR(PupilMax, radiusSpeed * 3f)
                    pupilIsApproachingMin = true
                } else {
                    sight.approachR(PupilMin, radiusSpeed)
                    if (sight.r - PupilMin <= radiusSpeed) {
                        pupilIsApproachingMin = false
                    }
                }
            }
            DebugOnly {
                G.dashCircle(x, y, stareAtScreenRadius, alpha = 0.2f)
            }
            WhenNotPaused {
                val payloadHolder = lastPayloadHolder
                if (consValid && (isShooting || isControlled || target != null || !isOutOfCombat)) {
                    sight.a = rotation.radian
                } else {
                    if (payloadHolder != null) {
                        sight.approachA(payloadHolder.rotation.radian, radiusSpeed * 3f)
                    } else {
                        val player = Vars.player.unit()
                        if (player.dst(this) > stareAtScreenRadius) {
                            val targetAngle = Angles.angle(x, y, player.x, player.y)
                            sight.a = targetAngle.radian
                        } else {
                            sight.approachR(0f, radiusSpeed * 3f)
                        }
                    }
                }
            }
            val isOutside = consValid && isShooting || sight.r > PupilMin * 1.5f
            val pupilTR = if (isOutside) PupilOutsideTR else PupilTR
            val pupilHeatTR = if (isOutside) PupilOutsideHeatTR else PupilHeatTR
            val pupilX = x + sight.x + recoilOffset.x
            val pupilY = y + sight.y + recoilOffset.y
            Draw.mixcol(eyeColor, heat)
            pupilTR.Draw(pupilX, pupilY, rotationDraw)
            Draw.z(Layer.turretHeat)
            heatMeta.drawHeat(heatShared) {
                pupilHeatTR.Draw(pupilX, pupilY, rotationDraw)
            }
            Draw.z(Layer.turret)
            Draw.z(Layer.turretHeat + 0.1f)
            drawHemorrhage()
            blinkAnime.draw(x, y)
            blinkFactor = 1f
        }

        open fun drawHemorrhage() {
            val hemorrhage = HemorrhageTRs.progress(continuousShots.toFloat() / maxHemorrhageShotsReq)
            Draw.alpha(heat)
            hemorrhage.Draw(x, y)
            Draw.color()
        }

        override fun handleBullet(bullet: Bullet, offsetX: Float, offsetY: Float, angleOffset: Float) {
            super.handleBullet(bullet, offsetX, offsetY, angleOffset)
            if (isLinkedBrain)
                improvedSounds.random().at(x, y, improvedSoundPitchRange.random(), soundVolume)
            else
                normalSounds.random().at(x, y, normalSoundPitchRange.random(), soundVolume)
        }

        override fun drawSelect() {
            G.dashCircleBreath(x, y, range() * smoothSelect(maxSelectedCircleTime), team.color, stroke = Var.CircleStroke)
        }

        override fun heal() {
            super.heal()
            trigger(Trigger.heal)
        }

        override fun heal(amount: Float) {
            super.heal(amount)
            trigger(Trigger.heal)
        }

        override fun hasAmmo() = true
        override fun useAmmo(): BulletType {
            ClientOnly {
                lastInCombatTime = 0f
                if (checkContinuousShooting())
                    continuousShots++
            }
            return if (isLinkedBrain) improvedBullet else normalBullet
        }

        override fun peekAmmo(): BulletType =
            if (isLinkedBrain) improvedBullet else normalBullet
    }
}