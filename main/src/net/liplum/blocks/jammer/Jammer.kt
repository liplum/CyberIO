package net.liplum.blocks.jammer

import arc.func.Prov
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.util.Time
import arc.util.Tmp
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.world.Block
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret
import mindustry.world.draw.DrawBlock
import net.liplum.R
import net.liplum.common.entity.Progress
import net.liplum.common.entity.Queue
import plumy.dsl.DrawLayer
import plumy.core.ClientOnly
import plumy.core.WhenNotPaused
import net.liplum.animation.Floating
import net.liplum.animation.add
import plumy.animation.ContextDraw.Draw
import net.liplum.utils.draw
import net.liplum.utils.sub
import plumy.core.assets.EmptyTR
import plumy.core.assets.TR
import plumy.core.math.*

open class Jammer(name: String) : ContinuousLiquidTurret(name) {
    @ClientOnly var dx = -15f
    @ClientOnly var dy = -12f
    @ClientOnly var waveMaxNumber = 3
    @ClientOnly var waveShootingTime = 30f
    /**
     * It's a magic number but the more means the slower.
     */
    @ClientOnly var stereoSpeed = 35f

    init {
        buildType = Prov { JammerBuild() }
    }

    open inner class JammerBuild : ContinuousLiquidTurretBuild() {
        @ClientOnly val stereos = if (Vars.headless) emptyArray()
        else Array(2) {
            Stereo(this).apply {
                pos.set(dx - dx * it * 2, dy)
                targetPos.set(pos)
                angleDis = targetPos.angle() - 90f
                rotation = 180f * it
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
                if (!bullets.isEmpty) shootingTime += Time.delta
                else shootingTime -= Time.delta
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
        override fun afterRead() {
            ClientOnly {
                for (s in stereos) {
                    s.pos.setAngle(s.angleDis + rotation)
                }
            }
        }
    }

    inner class DrawStereo : DrawBlock() {
        @JvmField var StereoTR = EmptyTR
        @JvmField var waveReloadTime = 10f
        @JvmField var waveSpeed = 0.014f
        @JvmField var waveShootingBoost = 1f
        @JvmField var waveRadius: Radius = 2.5f
        @JvmField var waveStereoOffset = 3f
        override fun load(block: Block) = block.run {
            super.load(this)
            StereoTR = this.sub("stereo")
        }

        val vs = Vec2()
        val ve = Vec2()
        val total = Vec2()
        val per = Vec2()
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
            vs.set(start.pos).add(start.floating)
            ve.set(end.pos).add(end.floating)
            total.set(ve).sub(vs)
            per.set(total).nor().scl(waveStereoOffset)
            vs += per
            ve -= per
            total.set(ve).sub(vs)
            val length = total.len()
            val color = Tmp.c1.set(R.C.FutureBlue)
            val realWaveSpeed = waveSpeed * (1f + (shootingTime / waveShootingTime) * waveShootingBoost)

            DrawLayer(Layer.effect) {
                waves.forEach {
                    WhenNotPaused {
                        it.progress += realWaveSpeed
                    }
                    val moving = Tmp.v1.set(total).setLength(length * it.progress.smooth)
                    val fadeAlpha = when {
                        it.progress <= 0.3f -> (it.progress / 0.3f).smooth
                        it.progress >= 0.7f -> ((1f - it.progress) / 0.3f).smooth
                        else -> 1f
                    }
                    color.a(fadeAlpha)
                    Draw.color(color)
                    Lines.circle(
                        x + vs.x + moving.x,
                        y + vs.y + moving.y,
                        waveRadius,
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