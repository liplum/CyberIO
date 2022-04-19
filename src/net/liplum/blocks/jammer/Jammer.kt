package net.liplum.blocks.jammer

import arc.Core
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.util.Time
import arc.util.Tmp
import arc.util.io.Reads
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.LaserTurret
import mindustry.world.consumers.ConsumeType
import mindustry.world.meta.BlockGroup
import net.liplum.CalledBySync
import net.liplum.ClientOnly
import net.liplum.SendDataPack
import net.liplum.draw
import net.liplum.lib.Draw
import net.liplum.lib.Observer
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.entity.Loop
import net.liplum.lib.entity.Queue
import net.liplum.utils.*

open class Jammer(name: String) : LaserTurret(name) {
    @ClientOnly lateinit var TurretTR: TR
    @ClientOnly lateinit var StereoTR: TR
    @ClientOnly lateinit var SonicWaveAnim: Animation
    @ClientOnly var dx = -15f
    @ClientOnly var dy = -12f
    @ClientOnly var waveMaxNumber = 1
    @ClientOnly var waveShootingBoost = 1.3f
    @ClientOnly var waveShootingTime = 30f
    @ClientOnly var waveSpeed = 0.02f
    @ClientOnly var fadeInOut: FUNC = {
        when (it) {
            in 0f..0.1f -> it * 10f
            in 0.1f..0.9f -> 1f
            else -> 1f - (it - 0.9f) * 10f
        }
    }
    /**
     * It's a magic number but the more means the slower.
     */
    @ClientOnly var stereoSpeed = 35f

    init {
        outlineIcon = false
        consumes.remove(ConsumeType.liquid)
        config(Integer::class.java) { obj: JammerBuild, i ->
            if (i.toInt() == 1)
                obj.onJamming()
        }
    }

    override fun load() {
        super.load()
        TurretTR = this.sub("turret")
        StereoTR = this.sub("stereo")
        SonicWaveAnim = this.autoAnim("sonic-wave", 6, 30f)
    }

    open inner class JammerBuild : LaserTurretBuild() {
        @SendDataPack
        val isShootingOb = Observer { wasShooting }.notify { b ->
            if (b) configure(1)
        }
        @ClientOnly
        lateinit var stereos: Array<Stereo>

        init {
            ClientOnly {
                stereos = Array(2) {
                    Stereo(this).apply {
                        image = StereoTR
                        pos.set(dx - dx * it * 2, dy)
                        targetPos.set(pos)
                        angleDis = targetPos.angle() - 90f
                        rotation = 180f * it
                    }
                }
            }
        }
        @CalledBySync
        open fun onJamming() {
            Groups.build.each {
                if (
                    it.team != team &&
                    it.block.group == BlockGroup.logic &&
                    it.dst(this) <= range() * 2
                ) {
                    it.destroyLogic()
                }
            }
        }
        @ClientOnly
        var shootingTime = 0f
            set(value) {
                field = value.coerceIn(0f, waveShootingTime)
            }

        override fun update() {
            super.update()
            isShootingOb.update()
            ClientOnly {
                if (wasShooting)
                    shootingTime += Time.delta
                else
                    shootingTime -= Time.delta
            }
        }
        @ClientOnly
        var progress = 0f
            set(value) {
                field = value % 1f
            }
        @ClientOnly
        val realWaveMaxNumber: Int
            get() = waveMaxNumber + if (wasShooting) 2 else 0
        @ClientOnly
        val waves: Queue<Loop> = Queue(this::realWaveMaxNumber::get) {
            Loop()
        }
        @ClientOnly
        val realWaveSpeed: Float
            get() = waveSpeed * (1f + (shootingTime / waveShootingTime) * waveShootingBoost)

        override fun draw() {
            baseRegion.Draw(x, y)
            Draw.color()

            Draw.z(Layer.turret)

            tr2.trns(rotation, -recoil)

            Drawf.shadow(TurretTR, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90)
            TurretTR.Draw(x + tr2.x, y + tr2.y, rotation - 90)

            if (Core.atlas.isFound(heatRegion)) {
                heatDrawer(this)
            }
            // Draw Stereos
            for (s in stereos) {
                s.targetPos.setAngle(s.angleDis + rotation)
                val force = Tmp.v1.set(s.targetPos).sub(s.pos)
                s.vel.set(force).setLength(Mathf.sqrt(force.len() / stereoSpeed))
                s.move(Time.delta)
                s.draw()
            }

            // Draw sonic wave
            val total = Tmp.v2.set(stereos[1].pos).sub(stereos[0].pos)
            val length = total.len()
            progress = Mathf.approach(progress, 1f, realWaveSpeed)
            total.setLength(length * progress)
            Draw.alpha(fadeInOut(progress))
            SonicWaveAnim.draw(
                x + stereos[0].pos.x + total.x,
                y + stereos[0].pos.y + total.y,
                rotation.draw
            )
            /*val per = total.scl(1f / waveNumber)
            var curX = stereos[0].pos.x + x
            var curY = stereos[0].pos.y + y
            for (i in 0 until waveNumber - 1) {
                curX += per.x
                curY += per.y
                if (i < waveNumber / 2 - 1 || i > waveNumber / 2) {
                    SonicWaveTRs[0].Draw(curX, curY, rotation.draw)
                }
            }*/
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            ClientOnly {
                for (s in stereos) {
                    s.pos.setAngle(s.angleDis + rotation)
                }
            }
        }
    }
}
@ClientOnly
class Stereo(val jammer: Jammer.JammerBuild) {
    lateinit var image: TR
    var rotation = 0f
    val pos = Vec2()
    val targetPos = Vec2()
    var angleDis = 0f
    val vel = Vec2()
    fun draw() {
        image.Draw(
            jammer.x + pos.x,
            jammer.y + pos.y,
            rotation + jammer.rotation.draw
        )
    }

    fun move(delta: Float) {
        pos.x += vel.x * delta
        pos.y += vel.y * delta
    }
}