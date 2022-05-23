package net.liplum.blocks.power

import net.liplum.blocks.power.WirelessTower.WirelessTowerBuild
import net.liplum.mdt.mixin.EntityMixin
import net.liplum.registries.EntityRegistry

class MagneticField : EntityMixin() {
    var tower: WirelessTowerBuild? = null
    override fun update() {
    }

    override fun classId() = EntityRegistry[this::class.java]
    override fun toString() = "MagneticField#${id()}"

    companion object {
        @JvmStatic
        fun create() =
            MagneticField()
        @JvmStatic
        fun create(tower: WirelessTowerBuild) =
            MagneticField().apply {
                this.tower = tower
            }
    }
}