package net.liplum.holo

import arc.Core
import arc.Events
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.game.EventType.UnitDestroyEvent
import mindustry.gen.UnitEntity
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.logic.LAccess
import mindustry.world.blocks.ConstructBlock.ConstructBuild
import net.liplum.mdt.ClientOnly
import net.liplum.R
import net.liplum.S
import net.liplum.lib.Serialized
import net.liplum.holo.HoloProjector.HoloPBuild
import net.liplum.registries.EntityRegistry
import net.liplum.mdt.render.G
import net.liplum.mdt.utils.build
import net.liplum.mdt.utils.exists
import net.liplum.mdt.utils.hasShields

open class HoloUnit : UnitEntity() {
    @Serialized
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
    @Serialized
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
        team.updateHoloCapacity()
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
                            S.Hologram, this
                        )
                    }
                }
            }
            kill()
        }
    }

    override fun draw() {
        drawBuilding()
        drawMining()
        drawStatusEffect()
        type.draw(this)
        drawRuvikTip()
    }

    override fun drawBuildingBeam(px: Float, py: Float) {
        val active = activelyBuilding()
        if (!active && lastActive == null) return
        Draw.z(Layer.flyingUnit)
        val plan = if (active) buildPlan() else lastActive
        val tile = Vars.world.tile(plan.x, plan.y)
        if (tile == null || !within(plan, if (Vars.state.rules.infiniteResources) Float.MAX_VALUE else type.buildRange)) {
            return
        }
        val size = if (plan.breaking) if (active) tile.block().size else lastSize else plan.block.size
        val tx = plan.drawx()
        val ty = plan.drawy()
        Lines.stroke(1.0f, S.Hologram)
        Draw.z(Layer.buildBeam)
        Draw.alpha(buildAlpha)
        if (!active && tile.build !is ConstructBuild) {
            Fill.square(plan.drawx(), plan.drawy(), size * Vars.tilesize / 2.0f)
        }
        Drawf.buildBeam(px, py, tx, ty, Vars.tilesize * size / 2.0f)
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
            Draw.color(S.Hologram)
            Draw.alpha(0.45f)
            Drawf.laser(Core.atlas.find("minelaser"), Core.atlas.find("minelaser-end"), px, py, ex, ey, 0.75f)
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
                G.drawDashCircle(x, y, holoType.ruvikTipRange, color = S.Hologram, alpha = ruvikTipAlpha)
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