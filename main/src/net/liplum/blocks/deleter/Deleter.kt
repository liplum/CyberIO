package net.liplum.blocks.deleter

import arc.Core
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.util.Eachable
import arc.util.Tmp
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.pattern.ShootSpread
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.gen.Healthc
import mindustry.gen.Hitboxc
import mindustry.world.Block
import mindustry.world.blocks.defense.turrets.PowerTurret
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.draw.DrawTurret
import mindustry.world.meta.Stat
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.api.IExecutioner
import net.liplum.draw
import net.liplum.lib.MapKeyBundle
import net.liplum.lib.bundle
import net.liplum.lib.mixin.shootPattern
import net.liplum.lib.ui.ammoStats
import net.liplum.utils.*

private val P2Alpha = quadratic(0.95f, 0.35f)

open class Deleter(name: String) : PowerTurret(name), IExecutioner {
    override var executeProportion: Float = 0.2f
    @JvmField var extraLostHpBounce = 0.01f
    @JvmField var waveType: DeleterWave

    init {
        val shootPattern = shootPattern(ShootSpread())
        shootPattern.shots = 18
        shootPattern.spread = 3f
        targetAir = true
        targetGround = true
        waveType = DeleterWave()
        shootType = waveType
    }

    open fun configBullet(config: DeleterWave.() -> Unit) {
        config(waveType)
    }
    @ClientOnly
    protected val bundleOverwrite by lazy {
        MapKeyBundle(Core.bundle).overwrite(
            "bullet.damage", "$contentType.${super.name}.stats.bullet.damage".bundle(
                "{0}",
                (extraLostHpBounce * 100).format(1)
            )
        )
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.ammo)
        stats.add(Stat.ammo, ammoStats(
            Pair(this, waveType),
            extra = {
                it.row()
                it.add("$contentType.${super.name}.stats.bullet.execution".bundle(
                    (executeProportion * 100).format(1)
                ))
            },
            bundle = bundleOverwrite
        ))
    }

    init {
        drawer = object : DrawTurret() {
            lateinit var HaloTR: TR
            override fun load(b: Block) {
                super.load(this@Deleter)
                HaloTR = this@Deleter.sub("halo")
            }

            override fun drawTurret(t: Turret, b: TurretBuild) = b.run {
                super.drawTurret(this@Deleter, this)
                Draw.color(team.color)
                Draw.rect(
                    HaloTR,
                    x + recoilOffset.x,
                    y + recoilOffset.y,
                    rotation.draw
                )
            }

            override fun drawPlan(block: Block, plan: BuildPlan, list: Eachable<BuildPlan>) {
                super.drawPlan(this@Deleter, plan, list)
                Draw.color(Vars.player.team().color)
                Draw.rect(HaloTR, plan.drawx(), plan.drawy())
            }
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

        override fun hitTile(b: Bullet, build: Building, x: Float, y: Float, initialHealth: Float, direct: Boolean) {
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