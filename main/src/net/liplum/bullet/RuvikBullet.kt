package net.liplum.bullet

import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.util.Nullable
import arc.util.Time
import mindustry.Vars
import mindustry.ai.types.MissileAI
import mindustry.entities.Mover
import mindustry.entities.Units
import mindustry.entities.bullet.BulletType
import mindustry.game.Team
import mindustry.gen.*
import mindustry.graphics.Drawf
import mindustry.world.blocks.ControlBlock
import mindustry.world.blocks.defense.turrets.BaseTurret
import plumy.core.MUnit
import net.liplum.utils.findPlayer

@Suppress("ClassName")
enum class STEM_VERSION {
    STEM1, STEM2
}

open class RuvikBullet : BulletType {
    @JvmField var stemVersion: STEM_VERSION = STEM_VERSION.STEM1
    @JvmField var maxRange = 120f
    @JvmField var maxRange2 = 120f * 120f
    @JvmField var arrowWidth = 5f
    @JvmField var arrowLength = 10f

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

        fun control(angle: Float) {
            ruvikAngle = angle
            controlled = true
        }
    }

    override fun init() {
        super.init()
        maxRange2 = maxRange * maxRange
    }

    @JvmField var STEMSystem = STEM()
    open fun ControlBySTEM1(b: Bullet) {
        val data = b.data
        if (data is Unitc) {
            val player = data.player
            if (player != null) {
                val unit = player.unit()
                val aimX = unit.aimX
                val aimY = unit.aimY
                if (checkOverMaxRange(b, unit.x, unit.y)) return
                STEMSystem.control(b.angleTo(aimX, aimY))
            } else {
                if (checkOverMaxRange(b, data.x, data.y)) return
                val aimX = data.aimX()
                val aimY = data.aimY()
                STEMSystem.control(b.angleTo(aimX, aimY))
            }
        } else if (data is ControlBlock && data.isControlled) {
            val player = data.unit()
            if (checkOverMaxRange(b, player.x, player.y)) return
            val aimX = player.aimX
            val aimY = player.aimY
            STEMSystem.control(b.angleTo(aimX, aimY))
        } else if (data is BaseTurret.BaseTurretBuild) {
            if (checkOverMaxRange(b, data.x, data.y)) return
            STEMSystem.control(data.rotation)
        }
    }

    open fun ControlBySTEM2(b: Bullet) {
        val data = b.data
        if (data is Player) {
            val unit = data.unit()
            if (checkOverMaxRange(b, unit.x, unit.y)) return
            val aimX = unit.aimX()
            val aimY = unit.aimY()
            STEMSystem.control(b.angleTo(aimX, aimY))
        } else {
            ControlBySTEM1(b)
        }
    }

    open fun checkOverMaxRange(b: Bullet, unitX: Float, unitY: Float): Boolean {
        if (b.dst2(unitX, unitY) >= maxRange2) {
            b.data = null
            b.vel.scl(0.5f)
            return true
        }
        return false
    }

    open fun CONTROL(b: Bullet) {
        when (stemVersion) {
            STEM_VERSION.STEM1 -> ControlBySTEM1(b)
            STEM_VERSION.STEM2 -> ControlBySTEM2(b)
        }
    }

    override fun draw(b: Bullet) {
        super.draw(b)
        Draw.color(trailColor)
        Drawf.tri(b.x, b.y, arrowWidth, arrowLength, b.rotation())
        Draw.color()
    }

    open fun updateRuvik(b: Bullet) {
        STEMSystem.reset()
        CONTROL(b)
        if (STEMSystem.controlled) {
            b.vel.setAngle(
                Angles.moveToward(
                    b.rotation(), STEMSystem.ruvikAngle,
                    speed * Time.delta * 50f
                )
            )
        }
    }

    override fun update(b: Bullet) {
        updateRuvik(b)
        super.update(b)
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
    @Nullable
    override fun create(
        owner: Entityc?,
        team: Team?,
        x: Float,
        y: Float,
        angle: Float,
        damage: Float,
        velocityScl: Float,
        lifetimeScl: Float,
        data: Any?,
        mover: Mover?,
        aimX: Float,
        aimY: Float,
    ): Bullet? {
        if (spawnUnit != null) {
            if (!Vars.net.client()) {
                val spawned = spawnUnit.create(team)
                spawned[x] = y
                spawned.rotation = angle
                //immediately spawn at top speed, since it was launched
                spawned.vel.trns(angle, spawnUnit.speed)
                //assign unit owner
                val controller = spawned.controller()
                if (controller is MissileAI && owner is MUnit) {
                    controller.shooter = owner
                }
            }
            //no bullet returned
            return null
        }
        val b = Bullet.create()
        b.type = this
        b.owner = owner
        b.team = team
        b.time = 0f
        b.originX = x
        b.originY = y
        b.aimTile = Vars.world.tileWorld(aimX, aimY)
        b.aimX = aimX
        b.aimY = aimY
        b.initVel(angle, speed * velocityScl)
        if (backMove) {
            b[x - b.vel.x * Time.delta] = y - b.vel.y * Time.delta
        } else {
            b[x] = y
        }
        b.lifetime = lifetime * lifetimeScl
        when (stemVersion) {
            STEM_VERSION.STEM1 -> b.data = data ?: owner
            STEM_VERSION.STEM2 -> {
                b.data = data ?: owner.findPlayer() ?: owner
            }
        }
        b.drag = drag
        b.hitSize = hitSize
        b.mover = mover
        b.damage = (if (damage < 0) this.damage else damage) * b.damageMultiplier()
        //reset trail
        if (b.trail != null) {
            b.trail.clear()
        }
        b.add()
        if (keepVelocity && owner is Velc)
            b.vel.add(owner.vel())
        return b
    }

    companion object {
        inline operator fun invoke(config: RuvikBullet.() -> Unit) =
            RuvikBullet().apply(config)
    }
}