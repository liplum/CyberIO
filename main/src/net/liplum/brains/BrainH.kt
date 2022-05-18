package net.liplum.brains

import mindustry.gen.Tex
import mindustry.world.Block
import mindustry.world.meta.Stat
import mindustry.world.meta.Stats
import net.liplum.api.brain.IComponentBlock
import net.liplum.api.brain.UpgradeType
import net.liplum.lib.utils.bundle
import net.liplum.mdt.ui.addTooltip

val UpgradeType.localizedName: String
    get() = "heimdall.${UpgradeType.I18ns[type].name}.name".bundle
val UpgradeType.description: String
    get() = "heimdall.${UpgradeType.I18ns[type].name}.description".bundle

fun <T> T.addUpgradeComponentStats() where T : Block, T : IComponentBlock {
    stats.add(Stat.boostEffect) { stat ->
        stat.row()
        for ((type, upgrade) in upgrades) {
            stat.add(type.localizedName).left().get().apply {
                addTooltip(type.description)
            }
            val i18n = UpgradeType.I18ns[type.type]
            stat.table {
                it.add(
                    if (upgrade.isDelta)
                        i18n.delta(upgrade.value)
                    else
                        i18n.percent(upgrade.value)
                ).minWidth(100f)
            }.get().background(Tex.underline)
            stat.row()
        }
    }
}

fun Stats.addHeimdallProperties(props: Map<UpgradeType, Float>) {
    add(Stat.boostEffect) { stat ->
        stat.row()
        for ((type, basic) in props) {
            stat.add(type.localizedName).left().get().apply {
                addTooltip(type.description)
            }
            val i18n = UpgradeType.I18ns[type.type]
            stat.table {
                it.add(i18n.delta(basic)
                    .replace("+", "")
                    .replace("-", ""))
                    .minWidth(100f)
            }.get().background(Tex.underline)
            stat.row()
        }
    }
}
