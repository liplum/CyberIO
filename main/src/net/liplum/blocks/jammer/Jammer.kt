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
import net.liplum.*
import net.liplum.lib.Draw
import net.liplum.lib.Observer
import net.liplum.lib.animations.Floating
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.entity.Progress
import net.liplum.lib.entity.Queue
import net.liplum.utils.*

open class Jammer(name: String) : LaserTurret(name) {
    @ClientOnly lateinit var TurretTR: TR
    @ClientOnly lateinit var StereoTR: TR
    @ClientOnly lateinit var SonicWaveAnim: Animation
    @ClientOnly var dx = -15f
    @ClientOnly var dy = -12f
    @ClientOnly var waveMaxNumber = 3
    @ClientOnly var waveShootingBoost = 1f
    @ClientOnly var waveShootingTime = 30f
    @ClientOnly var waveSpeed = 0.02f
    @ClientOnly var waveReloadTime = 10f
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

        override fun updateTile() {
            super.updateTile()
            isShootingOb.update()
            ClientOnly {
                if (bullet != null)
                    shootingTime += Time.delta
                else
                    shootingTime -= Time.delta
            }
        }
        @ClientOnly
        val realWaveMaxNumber: Int
            get() = waveMaxNumber + if(bullet!= null) 1 else 0
        @ClientOnly
        val waves: Queue<Progress> = Queue(this::realWaveMaxNumber::get) {
            Progress()
        }
        @ClientOnly
        var waveReload = 0f
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
                WhenNotPaused {
                    s.targetPos.setAngle(s.angleDis + rotation)
                    val force = Tmp.v1.set(s.targetPos).sub(s.pos)
                    s.vel.set(force).setLength(Mathf.sqrt(force.len() / stereoSpeed))
                    s.move(Time.delta)
                }
                s.draw()
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
                Tmp.v1.set(total).setLength(length * it.progress)
                Draw.alpha(fadeInOut(it.progress))
                SonicWaveAnim.draw(
                    x + start.pos.x + Tmp.v1.x,
                    y + start.pos.y + Tmp.v1.y,
                    rotation.draw
                )
            }
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
    val floating = Floating(0.8f).randomXY().changeRate(6)
    fun draw() {
        image.Draw(
            jammer.x + pos.x + floating.dx,
            jammer.y + pos.y + floating.dy,
            rotation + jammer.rotation.draw
        )
    }

    fun move(delta: Float) {
        floating.move(0.02f * delta)
        pos.x += vel.x * delta
        pos.y += vel.y * delta
    }
}