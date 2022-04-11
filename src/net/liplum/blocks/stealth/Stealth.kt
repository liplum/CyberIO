package net.liplum.blocks.stealth

import arc.graphics.Color
import arc.math.Mathf
import arc.struct.ObjectMap
import arc.struct.ObjectSet
import arc.struct.OrderedSet
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.entities.bullet.BulletType
import mindustry.gen.Building
import mindustry.gen.Groups
import mindustry.type.Liquid
import mindustry.world.Tile
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.meta.Stat
import mindustry.world.meta.StatValues
import net.liplum.api.cyber.IStreamClient
import net.liplum.api.cyber.IStreamHost
import net.liplum.api.cyber.req
import net.liplum.lib.delegates.Delegate1
import net.liplum.persistance.intSet
import net.liplum.registries.CioBulletTypes
import net.liplum.registries.CioLiquids.cyberion

open class Stealth(name: String) : Turret(name) {
    @JvmField var maxConnection = -1
    @JvmField var shootType: BulletType = CioBulletTypes.ruvik2
    @JvmField var activePower = 2.5f
    @JvmField var reactivePower = 0.5f

    init {
        hasLiquids = true
        hasPower = true
        update = true
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

    override fun setStats() {
        super.setStats()
        stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(this, shootType)))
    }

    open inner class StealthBuild : TurretBuild(), IStreamClient {
        var hosts = OrderedSet<Int>()
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

        override fun useAmmo(): BulletType {
            if (cheating()) return shootType
            liquids.remove(liquids.current(), 1f / shootType.ammoMultiplier)
            return shootType
        }

        override fun updateTile() {
            unit.ammo(unit.type().ammoCapacity * liquids.currentAmount() / liquidCapacity)
            super.updateTile()
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

        override fun peekAmmo() = shootType
        override fun acceptLiquid(source: Building, liquid: Liquid) = liquid == cyberion
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
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            hosts = read.intSet()
        }
    }
}