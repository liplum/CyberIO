package net.liplum.api.brain

import arc.graphics.Color
import mindustry.gen.Building
import mindustry.gen.Tex
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.meta.Stats
import net.liplum.R
import plumy.dsl.bundle
import net.liplum.common.util.toFloat
import plumy.dsl.AddBar
import net.liplum.registry.CioStats
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
                    val table = UpgradeStatTable(this)
                    if (upgrade.isDelta)
                        i18n.delta(table, upgrade)
                    else
                        i18n.percent(table, upgrade)
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
                    val table = UpgradeStatTable(this)
                    i18n.basic(table, basic)
                }.right().grow().pad(10f)
            }.growX().height(50f).pad(5f)
            stat.row()
        }
    }
}

inline fun <reified T> Block.addBrainLinkInfo() where T : Building, T : IUpgradeComponent {
    AddBar<T>(
        R.Bar.LinkedN,
        {
            if (isLinkedBrain) R.Bar.Linked.bundle
            else R.Bar.Unlinked.bundle
        },
        { R.C.Host },
        { isLinkedBrain.toFloat() }
    )
}
