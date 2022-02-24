package net.liplum.blocks.deleter

import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.util.Tmp
import mindustry.content.Fx
import mindustry.entities.bullet.BasicBulletType
import mindustry.gen.Bullet
import mindustry.gen.Healthc
import mindustry.gen.Hitboxc
import mindustry.world.blocks.defense.turrets.PowerTurret
import net.liplum.R

open class Deleter(name: String) : PowerTurret(name) {
    var executeProportion = 0.2f

    init {
        shots = 18
        spread = 3f
        targetAir = true
        targetGround = true
        shootType = DeleterWave().apply {
        }
    }

    open inner class DeleterWave : BasicBulletType() {
        init {
            hitEffect = Fx.hitLancer
            frontColor = R.C.Holo
            backColor = R.C.HoloDark
            pierce = true
            absorbable = true
            collidesAir = true
            collidesGround = true
            shrinkX = -5f
            shrinkY = 0f
            width = 10f
            height = 5f

            speed = 1.5f
            hitSize = 4f
            lifetime = 128f
            hitSize = 16f
            ammoMultiplier = 1f

            damage = 2f
        }

        override fun despawned(b: Bullet) {
        }

        override fun hitEntity(b: Bullet, entity: Hitboxc, health: Float) {
            if (entity is Healthc) {
                val e = entity as Healthc
                val h = e.health()
                val mh = e.maxHealth()
                if (h < mh * executeProportion) {
                    e.damage(mh)
                } else {
                    e.damage(b.damage)
                }
            }
        }

        override fun draw(b: Bullet) {
            drawTrail(b)
            val height = height * (1f - shrinkY + shrinkY * b.fout())
            val width = width * (1f - shrinkX + shrinkX * b.fout())
            val offset = -90 + if (spin != 0f) Mathf.randomSeed(b.id.toLong(), 360f) + b.time * spin else 0f
            val mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin())

            Draw.mixcol(mix, mix.a)
            val a = (b.fout() * 8f / 10f).coerceIn(0.5f, 0.9f)
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