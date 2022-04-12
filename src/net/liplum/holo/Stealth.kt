package net.liplum.holo

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.struct.ObjectMap
import arc.struct.ObjectSet
import arc.struct.OrderedSet
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.entities.bullet.BulletType
import mindustry.gen.Building
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.type.Liquid
import mindustry.ui.Bar
import mindustry.world.Tile
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.meta.Stat
import mindustry.world.meta.StatValues
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.api.cyber.IStreamClient
import net.liplum.api.cyber.IStreamHost
import net.liplum.api.cyber.req
import net.liplum.api.holo.IHoloEntity
import net.liplum.lib.animations.anis.Draw
import net.liplum.lib.delegates.Delegate1
import net.liplum.lib.shaders.use
import net.liplum.persistance.intSet
import net.liplum.registries.CioBulletTypes
import net.liplum.registries.CioLiquids.cyberion
import net.liplum.registries.CioShaders
import net.liplum.seconds
import net.liplum.utils.*

open class Stealth(name: String) : Turret(name) {
    @JvmField var restoreReload = 10 * 60f
    @JvmField var maxConnection = -1
    @JvmField var shootType: BulletType = CioBulletTypes.ruvik2
    @JvmField var activePower = 2.5f
    @JvmField var reactivePower = 0.5f
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var ImageTR: TR
    @ClientOnly lateinit var UnknownTR: TR
    @JvmField var minHealthProportion = 0.05f

    init {
        update = true
        sync = true
        //Hologram
        solid = false
        hasShadow = false
        absorbLasers = true
        floating = true
        //Turret
        hasLiquids = true
        hasPower = true
        acceptCoolant = false
        acceptsItems = false
        consumes.consumesLiquid(cyberion)
    }

    override fun init() {
        consumes.powerDynamic<StealthBuild> {
            if (it.isActive)
                activePower + reactivePower
            else
                reactivePower
        }
        super.init()
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        ImageTR = this.sub("image")
        UnknownTR = this.sub("unknown")
    }

    override fun icons() = arrayOf(UnknownTR)
    override fun setStats() {
        super.setStats()
        stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(cyberion, shootType)))
    }

    open inner class StealthBuild : TurretBuild(), IStreamClient, IHoloEntity {
        // Hologram
        var restoreCharge = restoreReload
        open var restRestore = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        open var lastDamagedTime = restoreReload
        override val minHealthProportion: Float
            get() = this@Stealth.minHealthProportion
        open val canRestructure: Boolean
            get() = lastDamagedTime > restoreReload
        open val canRestore: Boolean
            get() = health < maxHealth
        open val isRecovering: Boolean
            get() = restRestore > 0.5f
        // Turret
        var hosts = OrderedSet<Int>()
        override fun updateTile() {
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
                heal(restored)
                restRestore -= restored
            }

            if (canRestore && restoreCharge >= restoreReload) {
                restoreCharge = 0f
                if (health != maxHealth) {
                    dead = false
                    restRestore = maxHealth
                }
            }
            super.updateTile()
        }

        override fun draw() {
            Draw.z(Layer.blockUnder)
            Drawf.shadow(x, y, 10f)
            Draw.z(Layer.block)
            Draw.rect(BaseTR, x, y)
            CioShaders.Hologram.use(Layer.power) {
                val healthPct = healthPct
                it.alpha = healthPct / 4f * 3f
                it.opacityNoise *= 2f - healthPct
                it.flickering = it.DefaultFlickering + (1f - healthPct)
                it.blendHoloColorOpacity = 0f
                Draw.color(R.C.Holo)
                ImageTR.Draw(x, y)
                Draw.reset()
            }
            Draw.reset()
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

        override fun hasAmmo(): Boolean {
            return liquids[cyberion] >= 1f / shootType.ammoMultiplier
        }

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

        override fun bullet(type: BulletType, angle: Float) {
            val lifeScl = if (type.scaleVelocity)
                Mathf.clamp(
                    Mathf.dst(x + tr.x, y + tr.y, targetPos.x, targetPos.y) / type.range(),
                    minRange / type.range(),
                    range / type.range()
                )
            else 1f
            val nearestPlayer = Groups.player.find {
                it.team() == this.team && it.dst(this) <= range
            }
            type.create(
                this, team, x + tr.x, y + tr.y, angle, -1f,
                1f + Mathf.range(velocityInaccuracy), lifeScl, nearestPlayer
            )
        }

        override fun peekAmmo() = shootType
        override fun acceptLiquid(source: Building, liquid: Liquid) = false
        @JvmField var onRequirementUpdated: Delegate1<IStreamClient> = Delegate1()
        override fun getOnRequirementUpdated(): Delegate1<IStreamClient> = onRequirementUpdated
        override fun getRequirements(): Array<Liquid>? = cyberion.req
        override fun getConnectedHosts(): ObjectSet<Int> = hosts
        override fun getClientColor(): Color = cyberion.color
        override fun maxHostConnection() = maxConnection
        override fun getBuilding() = this
        override fun getTile(): Tile = tile
        override fun getBlock() = this@Stealth
        override fun write(write: Writes) {
            super.write(write)
            write.intSet(hosts)
            write.f(restoreCharge)
            write.f(restRestore)
            write.f(lastDamagedTime)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            hosts = read.intSet()
            restoreCharge = read.f()
            restRestore = read.f()
            lastDamagedTime = read.f()
        }
    }
    override fun setBars() {
        super.setBars()
        DebugOnly {
            bars.add<StealthBuild>(R.Bar.RestRestoreN) {
                Bar(
                    { R.Bar.RestRestore.bundle(it.restRestore.toInt()) },
                    { Pal.bar },
                    { it.restRestore / it.maxHealth }
                )
            }
            bars.add<StealthBuild>(R.Bar.ChargeN) {
                Bar(
                    { R.Bar.Charge.bundle(it.restoreCharge.seconds) },
                    { Pal.power },
                    { it.restoreCharge / restoreReload }
                )
            }
            bars.add<StealthBuild>(R.Bar.LastDamagedN) {
                Bar(
                    { R.Bar.LastDamaged.bundle(it.lastDamagedTime.seconds) },
                    { Pal.power },
                    { it.lastDamagedTime / restoreReload }
                )
            }
        }
    }
}