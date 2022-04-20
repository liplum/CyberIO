package net.liplum.holo

import arc.Core
import arc.Events
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.util.Structs
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.game.EventType.UnitDestroyEvent
import mindustry.gen.UnitEntity
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.logic.LAccess
import mindustry.world.blocks.ConstructBlock
import mindustry.world.blocks.ConstructBlock.ConstructBuild
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.holo.HoloProjector.HoloPBuild
import net.liplum.registries.EntityRegistry
import net.liplum.utils.G
import net.liplum.utils.build
import net.liplum.utils.exists
import net.liplum.utils.hasShields
import java.util.*

open class HoloUnit : UnitEntity() {
    @JvmField var time = 0f
    val HoloType: HoloUnitType
        get() = type as HoloUnitType
    open val lifespan: Float
        get() = HoloType.lifespan
    open val overageDmgFactor: Float
        get() = HoloType.overageDmgFactor
    open val lose: Float
        get() = HoloType.lose
    open val restLifePercent: Float
        get() = (1f - (time / lifespan)).coerceIn(0f, 1f)
    open val restLife: Float
        get() = (lifespan - time).coerceIn(0f, lifespan)
    open val loseMultiplierWhereMissing: Float
        get() = HoloType.loseMultiplierWhereMissing
    var projectorPos: Int = -1
    val isProjectorMissing: Boolean
        get() = !projectorPos.build.exists

    open fun setProjector(projector: HoloPBuild) {
        projectorPos = projector.pos()
    }

    override fun update() {
        val loseMultiplier: Float
        if (isProjectorMissing) {
            loseMultiplier = loseMultiplierWhereMissing
            projectorPos = -1
        } else {
            loseMultiplier = 1f
        }
        time += Time.delta * loseMultiplier
        super.update()
        val lose = lose * loseMultiplier
        var damage = lose
        val overage = time - lifespan
        if (overage > 0) {
            damage += overage * lose * overageDmgFactor
        }
        damageByHoloDimming(damage)
    }

    override fun destroy() {
        if (this.isAdded) {
            Events.fire(UnitDestroyEvent(this))
            for (mount in mounts) {
                if (mount.weapon.shootOnDeath && (!mount.weapon.bullet.killShooter || !mount.shoot)) {
                    mount.reload = 0.0f
                    mount.shoot = true
                    mount.weapon.update(this, mount)
                }
            }
            for (ability in abilities) {
                ability.death(this)
            }
            this.remove()
        }
    }

    override fun cap(): Int {
        return team.holoCapacity
    }

    open fun damageByHoloDimming(amount: Float) {
        val hasShields = this.hasShields
        if (hasShields) {
            shieldAlpha = 1.0f
        }
        health -= amount
        if (health <= 0.0f && !dead) {
            if (hasShields) {
                (abilities.find {
                    it is HoloForceField
                } as? HoloForceField)?.let {
                    val cacheRange = it.realRange(this)
                    Time.run(30f) {
                        HoloFx.shieldBreak.at(
                            x, y,
                            cacheRange,
                            R.C.Holo, this
                        )
                    }
                }
            }
            kill()
        }
    }

    override fun draw() {
        drawBuilding()
        type.draw(this)
        drawStatusEffect()
        drawMining()
        drawRuvikTip()
    }

    open fun drawBuilding() {
        val active = activelyBuilding()
        if (!active && lastActive == null) return
        Draw.z(Layer.flyingUnit)
        val plan = if (active)
            buildPlan()
        else
            lastActive
        val tile = Vars.world.tile(plan.x, plan.y)
        val core = team.core()
        if (tile == null ||
            !within(
                plan,
                if (Vars.state.rules.infiniteResources)
                    Float.MAX_VALUE
                else
                    Vars.buildingRange
            )
        ) {
            return
        }
        if (core != null && active && !isLocal && tile.block() !is ConstructBlock) {
            Draw.z(Layer.plans - 1.0f)
            drawPlan(plan, 0.5f)
            drawPlanTop(plan, 0.5f)
            Draw.z(Layer.flyingUnit)
        }
        val size =
            if (plan.breaking)
                if (active)
                    tile.block().size
                else
                    lastSize
            else
                plan.block.size
        val tx = plan.drawx()
        val ty = plan.drawy()
        Lines.stroke(
            1.0f, if (plan.breaking)
                R.C.HoloDark
            else
                R.C.Holo
        )
        val focusLen = type.buildBeamOffset + Mathf.absin(Time.time, 3.0f, 0.6f)
        val px = x + Angles.trnsx(rotation, focusLen)
        val py = y + Angles.trnsy(rotation, focusLen)
        val sz = Vars.tilesize * size / 2.0f
        val ang = angleTo(tx, ty)
        vecs[0].set(tx - sz, ty - sz)
        vecs[1].set(tx + sz, ty - sz)
        vecs[2].set(tx - sz, ty + sz)
        vecs[3].set(tx + sz, ty + sz)
        Arrays.sort(vecs, Structs.comparingFloat {
            -Angles.angleDist(
                angleTo(it),
                ang
            )
        })
        val close = Geometry.findClosest(x, y, vecs)
        val x1 = vecs[0].x
        val y1 = vecs[0].y
        val x2 = close.x
        val y2 = close.y
        val x3 = vecs[1].x
        val y3 = vecs[1].y
        Draw.z(Layer.buildBeam)
        Draw.color(R.C.Holo)
        Draw.alpha(buildAlpha)
        if (!active && tile.build !is ConstructBuild) {
            Fill.square(plan.drawx(), plan.drawy(), size * Vars.tilesize / 2.0f)
        }
        if (Vars.renderer.animateShields) {
            if (close != vecs[0] && close != vecs[1]) {
                Fill.tri(px, py, x1, y1, x2, y2)
                Fill.tri(px, py, x3, y3, x2, y2)
            } else {
                Fill.tri(px, py, x1, y1, x3, y3)
            }
        } else {
            Lines.line(px, py, x1, y1)
            Lines.line(px, py, x3, y3)
        }
        Fill.square(px, py, 1.8f + Mathf.absin(Time.time, 2.2f, 1.1f), rotation + 45)
        Draw.reset()
        Draw.z(Layer.flyingUnit)
    }

    open fun drawStatusEffect() {
        for (entry in statuses) {
            entry.effect.draw(this, entry.time)
        }
    }

    open fun drawMining() {
        if (mining()) {
            val focusLen = hitSize / 2.0f + Mathf.absin(Time.time, 1.1f, 0.5f)
            val swingScl = 12.0f
            val swingMag = 1.0f
            val px = x + Angles.trnsx(rotation, focusLen)
            val py = y + Angles.trnsy(rotation, focusLen)
            val ex = mineTile.worldx() + Mathf.sin(Time.time + 48.0f, swingScl, swingMag)
            val ey = mineTile.worldy() + Mathf.sin(Time.time + 48.0f, swingScl + 2.0f, swingMag)
            Draw.z(115.1f)
            Draw.color(R.C.Holo)
            Draw.alpha(0.45f)
            Drawf.laser(
                this.team(),
                Core.atlas.find("minelaser"),
                Core.atlas.find("minelaser-end"),
                px, py, ex, ey, 0.75f
            )
            if (this.isLocal) {
                Lines.stroke(1.0f)
                Lines.poly(mineTile.worldx(), mineTile.worldy(), 4, 4.0f * Mathf.sqrt2, Time.time)
            }
            Draw.color()
        }
    }
    @ClientOnly
    var ruvikTipAlpha = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    open fun drawRuvikTip() {
        val holoType = HoloType
        if (holoType.enableRuvikTip && isLocal) {
            if (isShooting) {
                ruvikTipAlpha += 2f / holoType.ruvikShootingTipTime
            } else {
                ruvikTipAlpha -= 0.5f / holoType.ruvikShootingTipTime
            }
            if (ruvikTipAlpha > 0f) {
                G.drawDashCircle(x, y, holoType.ruvikTipRange, color = R.C.Holo, alpha = ruvikTipAlpha)
            }
        }
    }

    override fun classId(): Int {
        return EntityRegistry.getID(javaClass)
    }

    override fun read(read: Reads) {
        super.read(read)
        time = read.f()
        projectorPos = read.i()
    }

    override fun write(write: Writes) {
        super.write(write)
        write.f(time)
        write.i(projectorPos)
    }

    override fun readSync(read: Reads) {
        super.readSync(read)
        time = read.f()
        projectorPos = read.i()
    }

    override fun writeSync(write: Writes) {
        super.writeSync(write)
        write.f(time)
        write.i(projectorPos)
    }

    override fun sense(sensor: LAccess): Double {
        return when (sensor) {
            LAccess.progress -> (1f - restLifePercent).toDouble()
            else -> super.sense(sensor)
        }
    }
}