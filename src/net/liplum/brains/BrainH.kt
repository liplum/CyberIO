package net.liplum.brains

import mindustry.gen.Tex
import mindustry.world.Block
import mindustry.world.meta.Stat
import mindustry.world.meta.Stats
import net.liplum.api.brain.IComponentBlock
import net.liplum.api.brain.UpgradeType
import net.liplum.utils.bundle
import net.liplum.utils.format
import net.liplum.utils.percent
import net.liplum.utils.value

val UpgradeType.localizedName: String
    get() = "heimdall.${UpgradeType.Names[type]}.name".bundle

fun <T> T.addUpgradeComponentStats() where T : Block, T : IComponentBlock {
    stats.add(Stat.boostEffect) { stat ->
        stat.row()
        for ((type, upgrade) in upgrades) {
            stat.add(type.localizedName).left()
            stat.table {
                it.add(
                    if (upgrade.isDelta)
                        upgrade.value.value()
                    else
                        upgrade.value.percent()
                )
            }.get().background(Tex.underline)
            stat.row()
        }
    }
}

fun Stats.addHeimdallProperties(props: Map<UpgradeType, Float>) {
    add(Stat.boostEffect) { stat ->
        stat.row()
        for ((type, basic) in props) {
            stat.add(type.localizedName).left()
            stat.table {
                it.add(basic.format(2))
            }.get().background(Tex.underline)
            stat.row()
        }
    }
}