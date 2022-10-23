package net.liplum.blocks.deleter

import arc.Core
import arc.func.Prov
import mindustry.world.blocks.defense.turrets.PowerTurret
import mindustry.world.meta.Stat
import net.liplum.common.util.MapKeyBundle
import net.liplum.common.util.format
import plumy.core.ClientOnly
import net.liplum.ui.ammoStats
import net.liplum.utils.subBundle

open class Deleter(name: String) : PowerTurret(name) {
    var executeProportion: Float = 0.2f
    @JvmField var extraLostHpBounce = 0.01f

    init {
        buildType = Prov { PowerTurretBuild() }
    }
    @ClientOnly
    protected val bundleOverwrite by lazy {
        MapKeyBundle(Core.bundle).overwrite(
            "bullet.damage",
            subBundle(
                "stats.bullet.damage",
                "{0}",
                (extraLostHpBounce * 100).format(1)
            )
        )
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.ammo)
        stats.add(
            Stat.ammo, ammoStats(
                Pair(this, shootType),
                extra = {
                    it.row()
                    it.add(
                        subBundle(
                            "stats.bullet.execution",
                            (executeProportion * 100).format(1)
                        )
                    )
                },
                bundle = bundleOverwrite
            )
        )
    }
}