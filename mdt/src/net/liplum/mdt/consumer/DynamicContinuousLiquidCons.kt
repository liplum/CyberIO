package net.liplum.mdt.consumer

import arc.scene.ui.layout.Table
import mindustry.gen.Building
import mindustry.type.LiquidStack
import mindustry.ui.ReqImage
import mindustry.world.consumers.Consume
import net.liplum.mdt.ui.LiquidImage

open class DynamicContinuousLiquidCons(
    val liquids: (Building) -> LiquidStack?,
) : Consume() {
    companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T : Building> create(cons: (T) -> LiquidStack?): DynamicContinuousLiquidCons {
            return DynamicContinuousLiquidCons(cons as (Building) -> LiquidStack?)
        }
    }

    override fun update(b: Building) {
        val req = liquids(b)
        if (req != null) {
            b.liquids.remove(req.liquid, req.amount * b.edelta())
        }
    }

    override fun efficiency(b: Building): Float {
        val req = liquids(b)
        return if (req != null) {
            (b.liquids.get(req.liquid) / req.amount * b.edelta())
                .coerceAtMost(1f)
        } else {
            0f
        }
    }

    override fun build(tile: Building, table: Table) {
        val current: Array<LiquidStack?> =
            arrayOf(liquids(tile))

        table.table { cont: Table ->
            table.update {
                if (current[0] != liquids(tile)) {
                    rebuild(tile, cont)
                    current[0] = liquids(tile)
                }
            }
            rebuild(tile, cont)
        }
    }

    open fun rebuild(tile: Building, table: Table) {
        table.clear()
        val req = liquids(tile)
        if (req != null) {
            table.add(ReqImage(
                LiquidImage(req.liquid.uiIcon, req.amount)
            ) {
                tile.liquids != null && tile.liquids[req.liquid] >= req.amount
            }).padRight(8f).left()
        }
    }
}