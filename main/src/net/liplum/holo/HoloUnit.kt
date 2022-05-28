package net.liplum.holo

import arc.Core
import arc.Events
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import arc.struct.Seq
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.ctype.Content
import mindustry.ctype.ContentType
import mindustry.game.EventType.UnitDestroyEvent
import mindustry.gen.UnitEntity
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.io.TypeIO
import mindustry.logic.LAccess
import mindustry.type.UnitType
import mindustry.world.blocks.ConstructBlock.ConstructBuild
import mindustry.world.blocks.payloads.BuildPayload
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.power.PowerGraph
import net.liplum.S
import net.liplum.holo.HoloProjector.HoloPBuild
import net.liplum.lib.Serialized
import net.liplum.lib.persistence.*
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.OverwriteVanilla
import net.liplum.mdt.mixin.PayloadMixin
import net.liplum.mdt.render.G
import net.liplum.mdt.utils.TE
import net.liplum.mdt.utils.build
import net.liplum.mdt.utils.exists
import net.liplum.mdt.utils.hasShields
import net.liplum.registries.CioLiquids
import net.liplum.registries.EntityRegistry

open class HoloUnit : UnitEntity(), PayloadMixin, IReverisonable {
    override fun revisionID() = 8
    override var payloadPower: PowerGraph? = null
    override var payloads = Seq<Payload>()
    override val unitType: UnitType
        get() = super.type
    @Serialized
    @JvmField var time = 0f
    val holoType: HoloUnitType
        get() = type as HoloUnitType
    open val lifespan: Float
        get() = holoType.lifespan
    open val overageDmgFactor: Float
        get() = holoType.overageDmgFactor
    open val lose: Float
        get() = holoType.lose
    open val restLifePercent: Float
        get() = (1f - (time / lifespan)).coerceIn(0f, 1f)
    open val restLife: Float
        get() = (lifespan - time).coerceIn(0f, lifespan)
    open val loseMultiplierWhereMissing: Float
        get() = holoType.loseMultiplierWhereMissing
    @Serialized
    var projectorPos: Int = -1
    val isProjectorMissing: Boolean
        get() = !projectorPos.build.exists

    open fun setProjector(projector: HoloPBuild) {
        projectorPos = projector.pos()
    }

    override fun toString() = "HoloUnit#$id"
    override fun update() {
        /* Pick up everything
        if (isPlayer) {
            val build = tileOn().build
            if (build != null)
                pickup(build)
        }*/
        if (type is HoloUnitType) {
            val loseMultiplier: Float
            if (isProjectorMissing) {
                loseMultiplier = loseMultiplierWhereMissing
                projectorPos = -1
            } else {
                loseMultiplier = 1f
            }
            time += Time.delta * loseMultiplier
            updateBySuper()
            val lose = lose * loseMultiplier
            var damage = lose
            val overage = time - lifespan
            if (overage > 0) {
                damage += overage * lose * overageDmgFactor
            }
            damageByHoloDimming(damage)
        } else {
            updateBySuper()
        }
    }

    open fun updateBySuper() {
        super.update()
        updatePayload()
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

    override fun killed() {
        super.killed()
        dropLastPayload()
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
        if (type is HoloUnitType) {
            val holoType = holoType
            if (holoType.enableRuvikTip && isLocal) {
                if (isShooting) {
                    ruvikTipAlpha += 2f / holoType.ruvikShootingTipTime
                } else {
                    ruvikTipAlpha -= 0.5f / holoType.ruvikShootingTipTime
                }
                if (ruvikTipAlpha > 0f) {
                    G.drawDashCircleBreath(x, y, holoType.ruvikTipRange, color = S.Hologram, alpha = ruvikTipAlpha)
                }
            }
        }
    }

    override fun updatePayload() {
        val projector = projectorPos.TE<HoloPBuild>()
        if (projector?.power != null)
            payloadPower = projector.power?.graph

        for (pay in payloads) {
            if (pay is BuildPayload && pay.build.power != null) {
                val payPower = payloadPower ?: PowerGraph()
                payloadPower = payPower
                pay.build.power.graph = null
                payPower.add(pay.build)
            }
        }
        payloadPower?.update()
        for (pay in payloads) {
            pay.set(x, y, rotation())
            tryTransferCyberionInto(pay)
            pay.update(self(), null)
        }
    }

    fun tryTransferCyberionInto(payload: Payload) {
        val projector = projectorPos.TE<HoloPBuild>() ?: return
        val type = type as? HoloUnitType ?: return
        if (payload is BuildPayload) {
            val build = payload.build
            if (build.acceptLiquid(projector, CioLiquids.cyberion)) {
                val amount = type.sacrificeCyberionAmount
                time += type.sacrificeLifeFunc(amount)
                build.handleLiquid(projector, CioLiquids.cyberion, amount)
            }
        }
    }

    override fun classId(): Int {
        return EntityRegistry[javaClass]
    }

    private fun readReversion7(read: Reads) {
        // Copy from parent revision 7
        abilities = TypeIO.readAbilities(read, abilities)
        ammo = read.f()
        controller = TypeIO.readController(read, controller)
        elevation = read.f()
        flag = read.d()
        health = read.f()
        isShooting = read.bool()
        mineTile = TypeIO.readTile(read)
        mounts = TypeIO.readMounts(read, mounts)
        plans = TypeIO.readPlansQueue(read)
        rotation = read.f()
        shield = read.f()
        spawnedByCore = read.bool()
        stack = TypeIO.readItems(read, stack)
        val statuses_LENGTH = read.i()
        statuses.clear()
        for (i in 0 until statuses_LENGTH) {
            val statusItem = TypeIO.readStatus(read)
            if (statusItem != null) {
                statuses.add(statusItem)
            }
        }
        team = TypeIO.readTeam(read)
        type = Vars.content.getByID<Content>(ContentType.unit, read.s().toInt()) as UnitType
        updateBuilding = read.bool()
        vel = TypeIO.readVec2(read, vel)
        x = read.f()
        y = read.f()
    }
    @OverwriteVanilla("Super")
    override fun read(_read_: Reads) {
        val REV = _read_.s().toInt()
        if (REV == 7) {
            readReversion7(_read_)
            time = _read_.f()
            projectorPos = _read_.i()
        } else if (REV >= 8) {
            // Since 8, use cache reader instead of vanilla
            ReadFromCache(_read_, revisionID()) {
                Warp {
                    readReversion7(this)
                }
                time = f()
                projectorPos = i()
                Warp {
                    readPayload(this)
                }
            }
        }
        this.afterRead()
    }

    private fun writeUnitEntity(write: Writes) {
        TypeIO.writeAbilities(write, abilities)
        write.f(ammo)
        TypeIO.writeController(write, controller)
        write.f(elevation)
        write.d(flag)
        write.f(health)
        write.bool(isShooting)
        TypeIO.writeTile(write, mineTile)
        TypeIO.writeMounts(write, mounts)
        write.i(plans.size)
        for (plan in plans) {
            TypeIO.writePlan(write, plan)
        }

        write.f(rotation)
        write.f(shield)
        write.bool(spawnedByCore)
        TypeIO.writeItems(write, stack)
        write.i(statuses.size)
        for (status in statuses) {
            TypeIO.writeStatus(write, status)
        }

        TypeIO.writeTeam(write, team)
        write.s(type.id.toInt())
        write.bool(updateBuilding)
        TypeIO.writeVec2(write, vel)
        write.f(x)
        write.f(y)
    }
    @OverwriteVanilla("Super")
    override fun write(_write_: Writes) {
        _write_.s(revisionID())
        // Since 8, use cache writer instead of vanilla
        WriteIntoCache(_write_, revisionID()) {
            Wrap {
                writeUnitEntity(this)
            }
            f(time)
            i(projectorPos)
            Wrap {
                writePayload(this)
            }
        }
    }
    // Sync doesn't need revision
    override fun readSync(read: Reads) {
        super.readSync(read)
        time = read.f()
        projectorPos = read.i()
        readPayload(read)
    }
    // Sync doesn't need revision
    override fun writeSync(write: Writes) {
        super.writeSync(write)
        write.f(time)
        write.i(projectorPos)
        writePayload(write)
    }

    override fun sense(sensor: LAccess): Double {
        return when (sensor) {
            LAccess.progress -> (1f - restLifePercent).toDouble()
            else -> super.sense(sensor)
        }
    }
}