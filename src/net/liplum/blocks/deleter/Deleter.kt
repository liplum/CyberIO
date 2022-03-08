package net.liplum.blocks.deleter

import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.util.Eachable
import arc.util.Tmp
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.gen.Healthc
import mindustry.gen.Hitboxc
import mindustry.world.blocks.defense.turrets.PowerTurret
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.api.IExecutioner
import net.liplum.draw
import net.liplum.utils.TR
import net.liplum.utils.lostHp
import net.liplum.utils.quadratic
import net.liplum.utils.sub

private val P2Alpha = quadratic(0.95f, 0.35f)

open class Deleter(name: String) : PowerTurret(name), IExecutioner {
    @ClientOnly lateinit var HaloTR: TR
    override var executeProportion: Float = 0.2f
    @JvmField var extraLostHpBounce = 0.01f
    @JvmField var waveType: DeleterWave

    init {
        shots = 18
        spread = 3f
        targetAir = true
        targetGround = true
        waveType = DeleterWave()
        shootType = waveType
    }

    override fun load() {
        super.load()
        HaloTR = this.sub("halo")
    }

    open fun configBullet(config: DeleterWave.() -> Unit) {
        config(waveType)
    }

    override fun drawRequestRegion(req: BuildPlan, list: Eachable<BuildPlan>) {
        super.drawRequestRegion(req, list)
        val team = Vars.player.team()
        Draw.color(team.color)
        Draw.rect(
            HaloTR,
            req.drawx(),
            req.drawy()
        )
        Draw.reset()
    }

    open inner class DeleterBuild : PowerTurretBuild() {
        override fun draw() {
            super.draw()
            Draw.color(team.color)
            Draw.rect(
                HaloTR,
                x + tr2.x,
                y + tr2.y,
                rotation.draw
            )
            Draw.reset()
        }
    }

    open inner class DeleterWave : BasicBulletType(), IExecutioner by this@Deleter {
        init {
            hitEffect = Fx.hitLancer
            frontColor = R.C.Holo
            backColor = R.C.HoloDark
            pierce = true
            pierceCap = 10
            lightRadius = 1f
            absorbable = false
            reflectable = false
            collidesAir = true
            collidesGround = true
            shrinkX = -5f
            shrinkY = 0f
            width = 10f
            height = 5f

            speed = 1.5f
            lifetime = 128f
            hitSize = 8f
            ammoMultiplier = 1f

            damage = 1f
        }

        override fun despawned(b: Bullet) {
        }

        open fun onHitTarget(b: Bullet, entity: Healthc) {
            if (entity.canBeExecuted) {
                execute(entity)
            } else {
                entity.damage(b.damage)
                entity.damagePierce(entity.lostHp * extraLostHpBounce)
            }
        }

        override fun hitTile(b: Bullet, build: Building, initialHealth: Float, direct: Boolean) {
            onHitTarget(b, build)
        }

        override fun hitEntity(b: Bullet, entity: Hitboxc, health: Float) {
            if (entity is Healthc) {
                onHitTarget(b, entity)
            }
        }

        override fun draw(b: Bullet) {
            drawTrail(b)
            val height = height * (1f - shrinkY + shrinkY * b.fout())
            val width = width * (1f - shrinkX + shrinkX * b.fout())
            val offset = -90 + if (spin != 0f) Mathf.randomSeed(b.id.toLong(), 360f) + b.time * spin else 0f
            val mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin())

            Draw.mixcol(mix, mix.a)
            val a = (P2Alpha(b.fout()))
            Draw.color(backColor)
            Draw.alpha(a)
            Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset)
            Draw.color(frontColor)
            Draw.alpha(a)
            Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset)

            Draw.reset()
        }
    }
}