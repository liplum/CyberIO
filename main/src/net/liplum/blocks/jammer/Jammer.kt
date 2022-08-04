package net.liplum.blocks.jammer

import arc.func.Prov
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.util.Time
import arc.util.Tmp
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret
import mindustry.world.draw.DrawBlock
import net.liplum.common.entity.Progress
import net.liplum.common.entity.Queue
import plumy.core.assets.TR
import plumy.core.math.nextBoolean
import plumy.core.math.smooth
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.WhenNotPaused
import net.liplum.mdt.animation.Floating
import net.liplum.mdt.animation.anims.Animation
import net.liplum.mdt.render.Draw
import net.liplum.mdt.render.DrawSize
import net.liplum.mdt.utils.autoAnim
import net.liplum.mdt.utils.draw
import net.liplum.mdt.utils.sub

open class Jammer(name: String) : ContinuousLiquidTurret(name) {
    @ClientOnly var dx = -15f
    @ClientOnly var dy = -12f
    @ClientOnly var waveMaxNumber = 3
    @ClientOnly var waveShootingBoost = 1f
    @ClientOnly var waveShootingTime = 30f
    @ClientOnly var waveSpeed = 0.014f
    @ClientOnly var waveReloadTime = 10f
    /**
     * It's a magic number but the more means the slower.
     */
    @ClientOnly var stereoSpeed = 35f

    init {
        buildType = Prov { JammerBuild() }
    }

    open inner class JammerBuild : ContinuousLiquidTurretBuild() {
        @ClientOnly
        lateinit var stereos: Array<Stereo>

        init {
            ClientOnly {
                stereos = Array(2) {
                    Stereo(this).apply {
                        pos.set(dx - dx * it * 2, dy)
                        targetPos.set(pos)
                        angleDis = targetPos.angle() - 90f
                        rotation = 180f * it
                    }
                }
            }
        }
        @ClientOnly
        var shootingTime = 0f
            set(value) {
                field = value.coerceIn(0f, waveShootingTime)
            }

        override fun updateTile() {
            super.updateTile()
            ClientOnly {
                if (!bullets.isEmpty)
                    shootingTime += Time.delta
                else
                    shootingTime -= Time.delta
            }
        }

        override fun shouldActiveSound(): Boolean {
            return wasShooting && enabled && !bullets.isEmpty
        }
        @ClientOnly
        val realWaveMaxNumber: Int
            get() = waveMaxNumber + if (!bullets.isEmpty) 1 else 0
        @ClientOnly
        val waves: Queue<Progress> = Queue(this::realWaveMaxNumber::get) {
            Progress()
        }
        @ClientOnly
        var waveReload = 0f
        @ClientOnly
        val realWaveSpeed: Float
            get() = waveSpeed * (1f + (shootingTime / waveShootingTime) * waveShootingBoost)

        override fun afterRead() {
            ClientOnly {
                for (s in stereos) {
                    s.pos.setAngle(s.angleDis + rotation)
                }
            }
        }
    }

    inner class DrawStereo : DrawBlock() {
        @ClientOnly lateinit var StereoTR: TR
        @ClientOnly lateinit var SonicWaveAnim: Animation
        override fun load(block: Block) = block.run {
            super.load(this)
            StereoTR = this.sub("stereo")
            SonicWaveAnim = this.autoAnim("sonic-wave", 6, 30f)
        }

        override fun draw(build: Building) = (build as JammerBuild).run {
            // Draw Stereos
            for (s in stereos) {
                WhenNotPaused {
                    s.targetPos.setAngle(s.angleDis + rotation)
                    val force = Tmp.v1.set(s.targetPos).sub(s.pos)
                    s.vel.set(force).setLength(Mathf.sqrt(force.len() / stereoSpeed))
                    s.move(Time.delta)
                }
                s.draw(StereoTR)
            }
            // Draw sonic wave
            WhenNotPaused {
                waveReload += delta()
            }
            if (waveReload >= waveReloadTime && waves.canAdd) {
                waveReload = 0f
                waves.append(Progress())
            }
            waves.pollWhen {
                it.progress >= 1f
            }
            val startIndex = Mathf.randomSeed(id.toLong(), 0, 1)
            val endIndex = 1 - startIndex
            val start = stereos[startIndex]
            val end = stereos[endIndex]
            val total = Tmp.v2.set(end.pos).sub(start.pos)
            val length = total.len()
            waves.forEach {
                WhenNotPaused {
                    it.progress += realWaveSpeed
                }
                Tmp.v1.set(total).setLength(length * it.progress.smooth)
                val fadeAlpha = when {
                    it.progress <= 0.2f -> (it.progress / 0.2f).smooth
                    it.progress >= 0.8f -> ((1f - it.progress) / 0.2f).smooth
                    else -> 1f
                }
                Draw.alpha(fadeAlpha)
                SonicWaveAnim.draw { wave->
                    wave.DrawSize(
                        x + start.pos.x + Tmp.v1.x,
                        y + start.pos.y + Tmp.v1.y,
                        rotation = rotation.draw,
                        size = 0.33333334f,
                    )
                }
            }
        }
    }
}
@ClientOnly
class Stereo(val jammer: Jammer.JammerBuild) {
    var rotation = 0f
    val pos = Vec2()
    val targetPos = Vec2()
    var angleDis = 0f
    val vel = Vec2()
    val floating = Floating(3f).apply {
        clockwise = nextBoolean()
        randomPos()
        changeRate = 10
    }

    fun draw(image: TR) {
        image.Draw(
            jammer.x + pos.x + floating.x,
            jammer.y + pos.y + floating.y,
            rotation + jammer.rotation.draw
        )
    }

    fun move(delta: Float) {
        floating.move(0.02f * delta)
        pos.x += vel.x * delta
        pos.y += vel.y * delta
    }
}