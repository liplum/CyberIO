package net.liplum.api.brain

import arc.graphics.Color
import mindustry.gen.Tex
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.meta.Stats
import net.liplum.common.utils.bundle
import net.liplum.registries.CioStats
import net.liplum.ui.addTable
import net.liplum.ui.then

val UpgradeType.localizedName: String
    get() = "heimdall.${UpgradeType.I18ns[type].name}.name".bundle
val UpgradeType.description: String
    get() = "heimdall.${UpgradeType.I18ns[type].name}.description".bundle

fun <T> T.addUpgradeComponentStats() where T : Block, T : IComponentBlock {
    stats.add(CioStats.heimdallImprove) { stat ->
        stat.row()
        for ((type, upgrade) in upgrades) {
            stat.addTable {
                background = Tex.whiteui
                setColor(Pal.darkestGray)
                addTable {
                    left()
                    add(type.localizedName).then {
                    }.padLeft(12f).left()
                    row()
                    add(type.description).then {
                        setColor(Color.lightGray)
                        setFontScale(0.9f)
                    }.padLeft(50f)
                }.left()
                val i18n = UpgradeType.I18ns[type.type]
                addTable {
                    right()
                    add(
                        if (upgrade.isDelta) i18n.delta(upgrade.value)
                        else i18n.percent(upgrade.value)
                    ).padRight(2f).right()
                }.right().grow().pad(10f)
            }.growX().height(50f).pad(5f)
            stat.row()
        }
    }
}

fun Stats.addHeimdallProperties(props: Map<UpgradeType, Float>) {
    add(CioStats.heimdallBase) { stat ->
        stat.row()
        for ((type, basic) in props) {
            stat.addTable {
                background = Tex.whiteui
                setColor(Pal.darkestGray)
                addTable {
                    left()
                    add(type.localizedName).then {
                    }.padLeft(12f).left()
                    row()
                    add(type.description).then {
                        setColor(Color.lightGray)
                        setFontScale(0.9f)
                    }.padLeft(50f)
                }.left()
                val i18n = UpgradeType.I18ns[type.type]
                addTable {
                    right()
                    add(
                        i18n.delta(basic)
                            .replace("+", "")
                            .replace("-", "")
                    ).padRight(2f).right()
                }.right().grow().pad(10f)
            }.growX().height(50f).pad(5f)
            stat.row()
        }
    }
}
