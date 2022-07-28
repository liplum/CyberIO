package net.liplum.mdt.consumer

import arc.scene.ui.layout.Table
import mindustry.gen.Building
import mindustry.type.LiquidStack
import mindustry.ui.ReqImage
import mindustry.world.consumers.Consume
import net.liplum.mdt.mixin.has
import net.liplum.common.util.toFloat
import net.liplum.mdt.ui.LiquidImage

open class DynamicOnceLiquidsCons(
    val liquids: (Building) -> Array<LiquidStack>,
) : Consume() {
    companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T : Building> create(cons: (T) -> Array<LiquidStack>): DynamicOnceLiquidsCons {
            return DynamicOnceLiquidsCons(cons as (Building) -> Array<LiquidStack>)
        }
    }

    override fun build(tile: Building, table: Table) {
        val current: Array<Array<LiquidStack>> =
            arrayOf(liquids(tile))

        table.table { cont: Table ->
            table.update {
                if (!current[0].contentEquals(liquids(tile))) {
                    rebuild(tile, cont)
                    current[0] = liquids(tile)
                }
            }
            rebuild(tile, cont)
        }
    }

    open fun rebuild(tile: Building, table: Table) {
        table.clear()
        for ((i, stack) in liquids(tile).withIndex()) {
            table.add(ReqImage(
                LiquidImage(stack.liquid.uiIcon, stack.amount)
            ) {
                tile.liquids != null && tile.liquids[stack.liquid] >= stack.amount
            }).padRight(8f).left()
            if ((i + 1) % 4 == 0)
                table.row()
        }
    }

    override fun trigger(entity: Building) {
        for (stack in liquids(entity)) {
            entity.liquids.remove(stack.liquid, stack.amount)
        }
    }

    override fun efficiency(b: Building): Float {
        val reqs = liquids(b)
        return b.liquids.has(reqs).toFloat()
    }
}