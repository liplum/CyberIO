package net.liplum.brains

import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Interp
import arc.util.Time
import mindustry.Vars
import mindustry.entities.bullet.BulletType
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.PowerTurret
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.WhenNotPaused
import net.liplum.api.brain.Direction4
import net.liplum.api.brain.IBrain
import net.liplum.api.brain.IUpgradeComponent
import net.liplum.draw
import net.liplum.lib.animations.anims.Anime
import net.liplum.lib.animations.anims.genFramesBy
import net.liplum.lib.animations.anis.Draw
import net.liplum.math.Polar
import net.liplum.utils.*

open class Eye(name: String) : PowerTurret(name) {
    lateinit var normalBullet: BulletType
    lateinit var improvedBullet: BulletType
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var EyeBallTR: TR
    @ClientOnly lateinit var EyelidTR: TR
    @ClientOnly lateinit var PupilTR: TR
    @ClientOnly lateinit var PupilOutsideTR: TR
    @ClientOnly lateinit var HemorrhageTRs: Array<TR>
    @ClientOnly lateinit var BlinkTRs: Array<TR>
    @ClientOnly @JvmField var BlinkDuration = 50f
    @ClientOnly @JvmField var BlinkFrameNum = 9
    @ClientOnly @JvmField var radiusSpeed = 0.1f
    @ClientOnly @JvmField var PupilMax = 5f
    @ClientOnly @JvmField var PupilMin = 1.2f
    @ClientOnly @JvmField var outOfCompactTime = 240f
    override fun init() {
        // To prevent accessing a null
        shootType = normalBullet
        super.init()
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        EyeBallTR = this.sub("eyeball")
        EyelidTR = this.sub("eyelid")
        BlinkTRs = this.sheet("blink", BlinkFrameNum)
        PupilTR = this.sub("pupil")
        PupilOutsideTR = this.sub("pupil-outside")
        HemorrhageTRs = this.sheet("hemorrhage", 3)
    }

    override fun icons() = arrayOf(
        BaseTR, EyelidTR
    )

    override fun setBars() {
        super.setBars()
        bars.addBrainInfo<EyeBuild>()
    }

    open inner class EyeBuild : PowerTurretBuild(), IUpgradeComponent {
        override var directionInfo: Direction4 = Direction4.Empty
        override var brain: IBrain? = null
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
                        forward || isShooting
                    }
                    onEnd = {
                        if (!isShooting) {
                            forward = !forward
                            isEnd = false
                        }
                    }
                }
            }
        }

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            clear()
        }

        override fun remove() {
            super.remove()
            clear()
        }
        @ClientOnly
        val stareAtScreenRadius: Float
            get() = size * Vars.tilesize * 2f
        @ClientOnly
        val sight = Polar(0f, 0f)
        @ClientOnly
        var lastInCombatTime = 0f
        @ClientOnly
        val isOutOfCombat: Boolean
            get() = lastInCombatTime > outOfCompactTime

        override fun draw() {
            WhenNotPaused {
                blinkAnime.spend(Time.delta)
                lastInCombatTime += Time.delta
            }
            BaseTR.Draw(x, y)
            Draw.color()

            Draw.z(Layer.turret)
            val rotationDraw = rotation.draw
            Drawf.shadow(EyeBallTR, x - elevation, y - elevation)
            EyeBallTR.Draw(x, y)
            val radiusSpeed = radiusSpeed * Time.delta
            if (isShooting) {
                sight.approachR(PupilMax, radiusSpeed * 3f)
            } else {
                sight.approachR(PupilMin, radiusSpeed)
            }
            DebugOnly {
                G.dashCircle(x, y, stareAtScreenRadius)
            }
            WhenNotPaused {
                if (isShooting || isControlled || target != null || !isOutOfCombat) {
                    sight.a = rotation.radian
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
            val pupil = if (isShooting) PupilOutsideTR else PupilTR
            var pupilX = x + sight.x
            var pupilY = y + sight.y
            if (isLinkedBrain) {
                tr2.trns(rotation, -recoil)
                pupilX += tr2.x
                pupilY += tr2.y
            }
            pupil.Draw(pupilX, pupilY, rotationDraw)
            drawHemorrhage()
            blinkAnime.draw(x, y)
        }

        open fun drawHemorrhage() {
            val index = when (timeScale) {
                in 0f..1f -> 0
                in 1.01f..2.499f -> 1
                else -> 2
            }
            val hemorrhage = HemorrhageTRs[index]
            Draw.alpha(heat * 1.2f)
            hemorrhage.Draw(x, y)
            Draw.color()
        }

        override fun hasAmmo() = true
        override fun useAmmo(): BulletType {
            ClientOnly {
                lastInCombatTime = 0f
            }
            return if (isLinkedBrain) improvedBullet else normalBullet
        }

        override fun peekAmmo(): BulletType =
            if (isLinkedBrain) improvedBullet else normalBullet
    }
}