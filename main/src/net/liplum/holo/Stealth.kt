package net.liplum.holo

import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.struct.ObjectMap
import arc.struct.ObjectSet
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.content.Bullets
import mindustry.entities.Damage
import mindustry.entities.bullet.BulletType
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.type.Liquid
import mindustry.world.Block
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.draw.DrawTurret
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import mindustry.world.meta.StatValues
import net.liplum.DebugOnly
import net.liplum.Var
import net.liplum.animation.Floating
import net.liplum.api.cyber.*
import net.liplum.api.holo.IHoloEntity
import net.liplum.api.holo.IHoloEntity.Companion.addHoloChargeTimeStats
import net.liplum.api.holo.IHoloEntity.Companion.addHoloHpAtLeastStats
import net.liplum.api.holo.IHoloEntity.Companion.minHealth
import net.liplum.bullet.RuvikBullet
import net.liplum.common.delegate.Delegate1
import net.liplum.common.persistence.read
import net.liplum.common.persistence.write
import net.liplum.common.shader.use
import net.liplum.consumer.LiquidTurretCons
import net.liplum.render.G
import net.liplum.utils.*
import net.liplum.registry.CioFluid.cyberion
import net.liplum.registry.SD
import plumy.animation.ContextDraw.Draw
import plumy.core.ClientOnly
import plumy.core.Serialized
import plumy.core.WhenNotPaused
import plumy.core.arc.toSecond
import plumy.core.assets.TR
import plumy.core.math.isZero
import plumy.core.math.nextBoolean
import plumy.dsl.AddBar

open class Stealth(name: String) : Turret(name) {
    @JvmField var restoreChargeTime = 10 * 60f
    @JvmField var maxConnection = -1
    @JvmField var shootType: BulletType = Bullets.placeholder
    @JvmField var activePower = 2.5f
    @JvmField var reactivePower = 0.5f
    @JvmField var minHealthProportion = 0.05f
    @ClientOnly @JvmField var FloatingRange = 2f
    @JvmField var restoreReq = 30f
    @ClientOnly @JvmField var ruvikShootingTipTime = 60f
    @JvmField val CheckConnectionTimer = timers++

    init {
        buildType = Prov { StealthBuild() }
        update = true
        sync = true
        //Hologram
        solid = false
        hasShadow = false
        absorbLasers = true
        floating = true
        teamPassable = true
        //Turret
        hasLiquids = true
        hasPower = true
        acceptsItems = false
    }

    override fun init() {
        consume(LiquidTurretCons(cyberion))
        consumePowerDynamic<StealthBuild> {
            if (it.isActive) activePower + reactivePower
            else reactivePower
        }
        super.init()
    }

    override fun setStats() {
        super.setStats()
        stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(cyberion, shootType)))
        stats.remove(Stat.powerUse)
        stats.add(Stat.powerUse) {
            it.add("${(reactivePower * 60f).toInt()} + ${(activePower * 60f).toInt()} ${StatUnit.powerSecond.localized()}")
        }
        addHoloChargeTimeStats(restoreChargeTime)
        addHoloHpAtLeastStats(minHealthProportion)
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        this.drawLinkedLineToClientWhenConfiguring(x, y)
    }

    init {
        drawer = object : DrawTurret() {
            lateinit var BaseTR: TR
            lateinit var ImageTR: TR
            lateinit var UnknownTR: TR
            override fun load(block: Block) = block.run {
                super.load(block)
                BaseTR = this.sub("base")
                ImageTR = this.sub("image")
                UnknownTR = region
            }

            override fun draw(build: Building) = (build as StealthBuild).run {
                WhenNotPaused {
                    val d = (0.1f * FloatingRange * delta() * (2f - healthPct)) * G.sclx
                    floating.move(d * 0.3f)
                }
                Draw.z(Layer.blockUnder)
                Drawf.shadow(x, y, 10f)
                Draw.z(Layer.block)
                Draw.rect(BaseTR, x, y)
                if (isProjecting) {
                    SD.Hologram.use(Layer.power) {
                        val healthPct = healthPct
                        it.alpha = healthPct / 4f * 3f
                        it.opacityNoise *= 2f - healthPct
                        it.flickering = it.DefaultFlickering + (1f - healthPct)
                        it.blendHoloColorOpacity = 0f
                        Draw.color(Var.Hologram)
                        ImageTR.Draw(
                            x + recoilOffset.x + floating.x,
                            y + recoilOffset.y + floating.y,
                            rotation.draw
                        )
                        Draw.reset()
                    }
                }
                if (unit.isLocal && shootType is RuvikBullet) {
                    if (isShooting) {
                        ruvikTipAlpha += 2f / ruvikShootingTipTime
                    } else {
                        ruvikTipAlpha -= 0.5f / ruvikShootingTipTime
                    }
                    if (ruvikTipAlpha > 0f) {
                        G.dashCircleBreath(x, y, range, color = Var.Hologram, alpha = ruvikTipAlpha)
                    }
                }
            }
        }
    }

    open inner class StealthBuild : TurretBuild(), IStreamClient, IHoloEntity {
        // Hologram
        @Serialized
        var restoreCharge = restoreChargeTime
        @Serialized
        override var restRestore = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @Serialized
        open var lastDamagedTime = restoreChargeTime
        override val minHealthProportion: Float
            get() = this@Stealth.minHealthProportion
        open val canRestructure: Boolean
            get() = lastDamagedTime > restoreChargeTime
        open val canRestore: Boolean
            get() = health < maxHealth
        open val isRecovering: Boolean
            get() = restRestore > 0.5f
        open val isProjecting: Boolean
            get() = health > minHealth
        open val curCyberionReq: Float
            get() = lostHpPct * restoreReq

        override fun collide(other: Bullet): Boolean =
            isProjecting ||
                    // Or isn't projecting but has not enough cyberion
                    (!isProjecting && liquids[cyberion] < curCyberionReq)
        // Turret
        @Serialized
        var hosts = OrderedSet<Int>()
        override fun updateTile() {
            // Check connection every second
            if (timer(CheckConnectionTimer, 60f)) {
                checkHostsPos()
            }
            unit.ammo(unit.type().ammoCapacity * liquids.currentAmount() / liquidCapacity)
            lastDamagedTime += delta()
            if (restoreCharge < restoreChargeTime && !isRecovering && canRestructure) {
                restoreCharge += delta()
            }
            if (isRecovering) {
                val restored = if (restRestore <= maxHealth * minHealthProportion)
                    restRestore
                else
                    restRestore * delta() * 0.01f
                health = health.coerceAtLeast(0f)
                if (restored > 0.001f) {
                    heal(restored)
                    restRestore -= restored
                }
            }
            if (canRestore && restoreCharge >= restoreChargeTime) {
                val restoreReq = curCyberionReq
                if (liquids[cyberion] >= curCyberionReq) {
                    restoreCharge = 0f
                    if (health != maxHealth) {
                        dead = false
                        restRestore = maxHealth
                        liquids.remove(cyberion, restoreReq)
                    }
                }
            }
            if (isProjecting) {
                super.updateTile()
            }
        }

        override fun canControl() =
            playerControllable && isProjecting

        override fun damage(damage: Float) {
            if (!this.dead()) {
                val dm = Vars.state.rules.blockHealth(team)
                var d = damage
                if (dm.isZero) {
                    d = this.health + 1.0f
                } else {
                    d /= Damage.applyArmor(damage, armor) / dm
                }
                d = handleDamage(d)
                val restHealth = health - d
                lastDamagedTime = 0f
                // Check whether it has enough cyberion
                val cyberionEnough = liquids[cyberion] >= curCyberionReq
                val realRestHealth = if (cyberionEnough) restHealth.coerceAtLeast(minHealth) else restHealth
                if (!Vars.net.client()) {
                    this.health = realRestHealth
                }
                healthChanged()
                if (this.health <= 0.0f) {
                    Call.buildDestroyed(this)
                }
            }
        }
        @ClientOnly
        var ruvikTipAlpha = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        @ClientOnly @JvmField
        var floating: Floating = Floating(FloatingRange).apply {
            clockwise = nextBoolean()
            randomPos()
            changeRate = 10
        }

        override fun drawSelect() {
            G.dashCircleBreath(x, y, range, Var.HologramDark)
            whenNotConfiguringP2P {
                this.drawStreamGraph()
            }
            this.drawRequirements()
        }

        override fun killThoroughly() {
            kill()
        }

        override fun drawCracks() {
        }

        override fun useAmmo(): BulletType {
            if (cheating()) return shootType
            liquids.remove(liquids.current(), 1f / shootType.ammoMultiplier)
            return shootType
        }

        override fun hasAmmo(): Boolean = liquids[cyberion] >= 1f / shootType.ammoMultiplier
        override fun readStreamFrom(host: IStreamHost, liquid: Liquid, amount: Float) {
            if (this.isConnectedTo(host)) {
                liquids.add(liquid, amount)
            }
        }

        override fun getAcceptedAmount(host: IStreamHost, liquid: Liquid): Float {
            return if (liquid == cyberion)
                liquidCapacity - liquids[cyberion]
            else
                0f
        }

        override fun handleBullet(bullet: Bullet, offsetX: Float, offsetY: Float, angleOffset: Float) {
            super.handleBullet(bullet, offsetX, offsetY, angleOffset)
            val nearestPlayer = if (isControlled) {
                unit().findPlayer()
            } else {
                Groups.player.find {
                    it.team() == team && it.dst(this) <= range
                }
            }
            bullet.data = nearestPlayer
        }

        override fun peekAmmo() = shootType
        override val onRequirementUpdated: Delegate1<IStreamClient> = Delegate1()
        override val requirements: Seq<Liquid>? = cyberion.req
        override val connectedHosts: ObjectSet<Int> = hosts
        override val clientColor: Color = cyberion.color
        override val maxHostConnection = maxConnection
        override fun write(write: Writes) {
            super.write(write)
            hosts.write(write)
            write.f(restoreCharge)
            write.f(restRestore)
            write.f(lastDamagedTime)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            hosts.read(read)
            restoreCharge = read.f()
            restRestore = read.f()
            lastDamagedTime = read.f()
        }
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            AddBar<StealthBuild>("rest-restore",
                { "Rest Restore:${restRestore.toInt()}" },
                { Pal.bar },
                { restRestore / maxHealth }
            )
            AddBar<StealthBuild>("charge",
                { "Charge: ${restoreCharge.toSecond}" },
                { Pal.power },
                { restoreCharge / restoreChargeTime }
            )
            AddBar<StealthBuild>("last-damage",
                { "Last Damage:${lastDamagedTime.toSecond}s" },
                { Pal.power },
                { lastDamagedTime / restoreChargeTime }
            )
        }
    }
}