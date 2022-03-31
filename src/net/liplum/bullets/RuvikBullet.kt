package net.liplum.bullets

import arc.math.Angles
import arc.math.Mathf
import arc.util.Nullable
import arc.util.Time
import mindustry.entities.Units
import mindustry.entities.bullet.BasicBulletType
import mindustry.game.Team
import mindustry.gen.*
import mindustry.world.blocks.ControlBlock
import mindustry.world.blocks.defense.turrets.BaseTurret
import net.liplum.utils.NullOr
import net.liplum.utils.findPlayer

@Suppress("ClassName")
enum class STEM_VERSION {
    STEM1, STEM2
}

open class RuvikBullet : BasicBulletType {
    @JvmField var stemVersion: STEM_VERSION = STEM_VERSION.STEM1

    constructor(speed: Float, damage: Float) : super(speed, damage)
    constructor() : super()

    data class STEM(
        var ruvikAngle: Float = 0f,
        var controlled: Boolean = false,
        //var distance: Float = -1f,
    ) {
        fun reset() {
            ruvikAngle = 0f
            controlled = false
        }

        fun control(
            angle: Float,
            //dis: Float = -1f,
        ) {
            ruvikAngle = angle
            controlled = true
            //distance = dis
        }
    }

    @JvmField var STEMSystem = STEM()
    open fun ControlBySTEM1(b: Bullet) {
        val data = b.data
        if (data is Unitc) {
            val player = data.player
            if (player != null) {
                val aimX = player.unit().aimX
                val aimY = player.unit().aimY
                STEMSystem.control(b.angleTo(aimX, aimY), /*b.dst(aimX, aimY)*/)
            } else {
                val aimX = data.aimX()
                val aimY = data.aimY()
                STEMSystem.control(b.angleTo(aimX, aimY), /*b.dst(aimX, aimY)*/)
            }
        } else if (data is ControlBlock && data.isControlled) {
            val player = data.unit()
            val aimX = player.aimX
            val aimY = player.aimY
            STEMSystem.control(b.angleTo(aimX, aimY), /*b.dst(aimX, aimY)*/)
        } else if (data is BaseTurret.BaseTurretBuild) {
            STEMSystem.control(data.rotation)
        }
    }

    open fun ControlBySTEM2(b: Bullet) {
        val data = b.data
        if (data is Player) {
            val unit = data.unit()
            val aimX = unit.aimX()
            val aimY = unit.aimY()
            STEMSystem.control(b.angleTo(aimX, aimY), /*b.dst(aimX, aimY)*/)
        } else {
            ControlBySTEM1(b)
        }
    }

    open fun CONTROL(b: Bullet) {
        when (stemVersion) {
            STEM_VERSION.STEM1 -> ControlBySTEM1(b)
            STEM_VERSION.STEM2 -> ControlBySTEM2(b)
        }
    }

    override fun update(b: Bullet) {
        updateTrail(b)
        STEMSystem.reset()
        CONTROL(b)
        if (STEMSystem.controlled) {
            b.vel.setAngle(
                Angles.moveToward(
                    b.rotation(), STEMSystem.ruvikAngle,
                    speed * Time.delta * 50f
                )
            )
            /* Taking distance of unit and destination into account
             val dis = STEMSystem.distance
             if (dis >= 0f) {
                 val len = b.vel.len()
                 b.vel.setLength(len + Mathf.log(100f,dis))
             }*/
        } else if (homingPower > 0.0001f && b.time >= homingDelay) {
            val target: Teamc? = findNormalTarget(b)
            if (target != null) {
                b.vel.setAngle(
                    Angles.moveToward(
                        b.rotation(), b.angleTo(target),
                        homingPower * Time.delta * 50f
                    )
                )
            }
        }

        if (weaveMag > 0) {
            b.vel.rotate(
                Mathf.sin(
                    b.time + Mathf.PI * weaveScale / 2f,
                    weaveScale,
                    weaveMag * if (Mathf.randomSeed(b.id.toLong(), 0, 1) == 1) -1 else 1
                ) * Time.delta
            )
        }

        if (trailChance > 0) {
            if (Mathf.chanceDelta(trailChance.toDouble())) {
                trailEffect.at(b.x, b.y, if (trailRotation) b.rotation() else trailParam, trailColor)
            }
        }

        if (trailInterval > 0f) {
            if (b.timer(0, trailInterval)) {
                trailEffect.at(b.x, b.y, if (trailRotation) b.rotation() else trailParam, trailColor)
            }
        }
    }

    open fun findNormalTarget(b: Bullet): Teamc? {
        //home in on allies if possible
        return if (healPercent > 0)
            Units.closestTarget(null, b.x, b.y, homingRange,
                {
                    it.checkTarget(
                        collidesAir,
                        collidesGround
                    ) && it.team !== b.team && !b.hasCollided(it.id)
                }
            ) {
                collidesGround && (it.team !== b.team || it.damaged()) && !b.hasCollided(it.id)
            }
        else
            Units.closestTarget(b.team, b.x, b.y, homingRange,
                {
                    it.checkTarget(
                        collidesAir,
                        collidesGround
                    ) && !b.hasCollided(it.id)
                }
            ) {
                collidesGround && !b.hasCollided(it.id)
            }
    }

    override fun create(
        @Nullable owner: Entityc?,
        team: Team,
        x: Float,
        y: Float,
        angle: Float,
        damage: Float,
        velocityScl: Float,
        lifetimeScl: Float,
        data: Any?
    ): Bullet {
        val b = Bullet.create()
        b.type = this
        b.owner = owner
        b.team = team
        b.time = 0f
        b.initVel(angle, speed * velocityScl)
        if (backMove) {
            b.set(x - b.vel.x * Time.delta, y - b.vel.y * Time.delta)
        } else {
            b.set(x, y)
        }
        b.lifetime = lifetime * lifetimeScl
        when (stemVersion) {
            STEM_VERSION.STEM1 -> b.data = data ?: owner
            STEM_VERSION.STEM2 -> {
                b.data = (data ?: owner.findPlayer()) NullOr owner
            }
        }
        b.drag = drag
        b.hitSize = hitSize
        b.damage = (if (damage < 0) this.damage else damage) * buildingDamageMultiplier
        //reset trail
        if (b.trail != null) {
            b.trail.clear()
        }
        b.add()
        if (keepVelocity && owner is Velc) {
            b.vel.add(owner.vel())
        }
        return b
    }
}