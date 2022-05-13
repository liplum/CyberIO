package net.liplum.brains

import arc.audio.Sound
import arc.graphics.g2d.TextureRegion
import mindustry.content.UnitTypes
import mindustry.gen.BlockUnitc
import mindustry.gen.Building
import mindustry.type.Liquid
import mindustry.world.blocks.ControlBlock
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.blocks.heat.HeatBlock
import mindustry.world.blocks.heat.HeatConsumer
import mindustry.world.draw.DrawMulti
import mindustry.world.draw.DrawRegion
import net.liplum.Serialized
import net.liplum.api.brain.*
import net.liplum.registries.CioSounds
import net.liplum.utils.MdtUnit

class Heart(name: String) : Turret(name), IComponentBlock {
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()
    // Normal
    @JvmField var normalBullet = BloodBullet.X
    @JvmField var normalHeartBeat: Sound = CioSounds.EmptySound
    @JvmField var normalPattern = HeartBeatShootPattern.X
    // Improved
    @JvmField var improvedBullet = BloodBullet.X
    @JvmField var improvedHeartBeat: Sound = CioSounds.EmptySound
    @JvmField var improvedPattern = HeartBeatShootPattern.X
    // Temperature
    @JvmField var coreHeat = 5f

    init {
        solid = true
        update = true
        hasPower = true
        sync = true
        canOverdrive = false
    }

    override fun init() {
        super.init()
    }

    var drawer = DrawMulti(
        DrawRegion("-base"),
        DrawRegion("-heart"),
    )

    override fun load() {
        super.load()
        drawer.load(this)
    }

    override fun setStats() {
        super.setStats()
        this.addUpgradeComponentStats()
    }

    override fun icons(): Array<TextureRegion> {
        return drawer.icons(this)
    }

    open inner class HeartBuild : TurretBuild(),
        IUpgradeComponent, ControlBlock, HeatConsumer, HeatBlock {
        override var directionInfo: Direction2 = Direction2()
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade>
            get() = this@Heart.upgrades
        var unit = UnitTypes.block.create(team) as BlockUnitc
        var heat = 0f
        /**
         *  Q = mcΔT
         */
        @Serialized
        var temperature = 0f
        @Serialized
        var bloodAmount = 0f
        override fun unit(): MdtUnit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as MdtUnit)
        }

        override fun draw() {
            drawer.draw(this)
        }
        /**
         * Heart doesn't allow gas
         */
        override fun acceptLiquid(source: Building, liquid: Liquid): Boolean {
            if (liquid.gas) return false
            return super.acceptLiquid(source, liquid)
        }

        override fun sideHeat(): FloatArray {
            TODO("Not yet implemented")
        }

        override fun heatRequirement(): Float {
            TODO("Not yet implemented")
        }

        override fun heat() = heat
        override fun heatFrac() = heat / coreHeat
    }
}