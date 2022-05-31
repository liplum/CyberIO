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
import net.liplum.R
import net.liplum.S
import net.liplum.api.cyber.*
import net.liplum.api.holo.IHoloEntity
import net.liplum.api.holo.IHoloEntity.Companion.minHealth
import net.liplum.bullets.RuvikBullet
import net.liplum.lib.Serialized
import net.liplum.lib.TR
import net.liplum.lib.delegates.Delegate1
import net.liplum.lib.persistence.read
import net.liplum.lib.persistence.write
import net.liplum.lib.shaders.SD
import net.liplum.lib.shaders.use
import net.liplum.lib.utils.isZero
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.Draw
import net.liplum.mdt.WhenNotPaused
import net.liplum.mdt.animations.Floating
import net.liplum.mdt.consumer.LiquidTurretCons
import net.liplum.mdt.render.G
import net.liplum.mdt.render.postToastTextOn
import net.liplum.mdt.ui.bars.AddBar
import net.liplum.mdt.utils.*
import net.liplum.registries.CioBulletTypes
import net.liplum.registries.CioLiquids.cyberion

open class Stealth(name: String) : Turret(name) {
    @JvmField var restoreReload = 10 * 60f
    @JvmField var maxConnection = -1
    @JvmField var shootType: BulletType = CioBulletTypes.ruvik2
    @JvmField var activePower = 2.5f
    @JvmField var reactivePower = 0.5f
    @JvmField var minHealthProportion = 0.05f
    @ClientOnly @JvmField var FloatingRange = 0.6f
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
            if (it.isActive)
                activePower + reactivePower
            else
                reactivePower
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
                    val d = G.D(0.1f * FloatingRange * delta() * (2f - healthPct))
                    floating.move(d)
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
                        Draw.color(S.Hologram)
                        ImageTR.Draw(
                            x + recoilOffset.x + floating.dx,
                            y + recoilOffset.y + floating.dy,
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
                        G.drawDashCircleBreath(x, y, range, color = S.Hologram, alpha = ruvikTipAlpha)
                    }
                }
            }
        }
    }

    open inner class StealthBuild : TurretBuild(), IStreamClient, IHoloEntity {
        // Hologram
        @Serialized
        var restoreCharge = restoreReload
        @Serialized
        override var restRestore = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @Serialized
        open var lastDamagedTime = restoreReload
        override val minHealthProportion: Float
            get() = this@Stealth.minHealthProportion
        open val canRestructure: Boolean
            get() = lastDamagedTime > restoreReload
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
            if (restoreCharge < restoreReload && !isRecovering && canRestructure) {
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
            if (canRestore && restoreCharge >= restoreReload) {
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
                    d /= dm
                }
                d = handleDamage(d)
                val restHealth = health - d
                lastDamagedTime = 0f
                // Check whether it has enough cyberion
                if (liquids[cyberion] >= curCyberionReq) {
                    Call.tileDamage(this, restHealth.coerceAtLeast(minHealth))
                } else {
                    Call.tileDamage(this, restHealth)

                    if (this.health <= 0.0f) {
                        Call.tileDestroyed(this)
                    }
                }
            }
        }
        @ClientOnly
        var ruvikTipAlpha = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        @ClientOnly @JvmField
        var floating: Floating = Floating(FloatingRange).randomXY().changeRate(1)
        override fun drawSelect() {
            G.dashCircleBreath(x, y, range, S.HologramDark)
            whenNotConfiguringHost {
                this.drawStreamGraphic()
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
        override fun readStream(host: IStreamHost, liquid: Liquid, amount: Float) {
            if (this.isConnectedWith(host)) {
                liquids.add(liquid, amount)
            }
        }

        override fun acceptedAmount(host: IStreamHost, liquid: Liquid): Float {
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
        override fun acceptLiquid(source: Building, liquid: Liquid): Boolean {
            ClientOnly {
                subBundle("unaccepted").postToastTextOn(this, R.C.RedAlert, overwrite = false)
            }
            return false
        }

        @JvmField var onRequirementUpdated: Delegate1<IStreamClient> = Delegate1()
        override fun getOnRequirementUpdated(): Delegate1<IStreamClient> = onRequirementUpdated
        override fun getRequirements(): Seq<Liquid>? = cyberion.req
        override fun getConnectedHosts(): ObjectSet<Int> = hosts
        override fun getClientColor(): Color = cyberion.color
        override fun maxHostConnection() = maxConnection
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
                { "Charge: ${restoreCharge.seconds}" },
                { Pal.power },
                { restoreCharge / restoreReload }
            )
            AddBar<StealthBuild>("last-damage",
                { "Last Damage:${lastDamagedTime.seconds}s" },
                { Pal.power },
                { lastDamagedTime / restoreReload }
            )
        }
    }
}