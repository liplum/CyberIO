package net.liplum.ui.bars

import arc.scene.ui.layout.Table
import arc.util.Time
import mindustry.Vars
import mindustry.gen.Building
import mindustry.ui.Bar
import mindustry.world.Block
import net.liplum.utils.LiquidTypeAmount
import plumy.dsl.ID

fun Block.removeItemsInBar() {
    this.removeBar("items")
}

fun Block.removeLiquidInBar() {
    this.removeBar("liquid")
}

fun Block.removeHealthInBar() {
    this.removeBar("health")
}

const val minIntervalBarDisplay = 10f
fun Block.genAllLiquidBars(): Array<(Building) -> Bar> =
    Array(LiquidTypeAmount()) { i ->
        val liquid = Vars.content.liquids()[i]
        {
            Bar({ liquid.localizedName },
                { liquid.barColor() },
                { it.liquids[liquid] / liquidCapacity }
            )
        }
    }

inline fun Building.appendDisplayLiquidsDynamic(
    table: Table,
    allLiquidBars: Array<(Building) -> Bar>,
    crossinline superDisplayBars: (Table) -> Unit,
) {
    table.update {
        if (Time.time % minIntervalBarDisplay < Time.delta) {
            table.clearChildren()
            superDisplayBars(table)
            for (liquid in Vars.content.liquids()) {
                if (liquids[liquid] > 0f) {
                    val bar = allLiquidBars[liquid.ID](this)
                    table.add(bar).growX()
                    table.row()
                }
            }
        }
    }
}