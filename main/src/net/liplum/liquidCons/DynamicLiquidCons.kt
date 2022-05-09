package net.liplum.liquidCons

import arc.func.Func
import arc.scene.ui.layout.Table
import arc.struct.Bits
import mindustry.gen.Building
import mindustry.type.LiquidStack
import mindustry.ui.ReqImage
import mindustry.world.consumers.Consume
import mindustry.world.consumers.ConsumeType
import mindustry.world.meta.Stats

open class DynamicLiquidCons(
    val liquids: Func<Building, Array<LiquidStack>>
) : Consume() {
    companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T : Building> create(cons: Func<T, Array<LiquidStack>>): DynamicLiquidCons {
            return DynamicLiquidCons(cons as Func<Building, Array<LiquidStack>>)
        }
    }

    override fun type(): ConsumeType = ConsumeType.liquid
    override fun applyLiquidFilter(filter: Bits) {
        //this must be done dynamically
    }

    override fun update(entity: Building) {
    }

    override fun build(tile: Building, table: Table) {
        val current: Array<Array<LiquidStack>> =
            arrayOf(liquids.get(tile))

        table.table { cont: Table ->
            table.update {
                if (!current[0].contentEquals(liquids.get(tile))) {
                    rebuild(tile, cont)
                    current[0] = liquids.get(tile)
                }
            }
            rebuild(tile, cont)
        }
    }

    open fun rebuild(tile: Building, table: Table) {
        table.clear()
        for ((i, stack) in liquids.get(tile).withIndex()) {
            table.add(ReqImage(
                LiquidImage(stack.liquid.uiIcon, stack.amount)
            ) {
                tile.liquids != null && tile.liquids[stack.liquid] >= stack.amount
            }).padRight(8f).left()
            if ((i + 1) % 4 == 0)
                table.row()
        }
    }

    override fun getIcon(): String {
        return "icon-liquid-consume"
    }

    override fun valid(entity: Building): Boolean {
        return entity.liquids != null &&
                entity.liquids.has(liquids.get(entity))
    }

    override fun trigger(entity: Building) {
        for (stack in liquids.get(entity)) {
            entity.liquids.remove(stack.liquid, stack.amount)
        }
    }

    override fun display(stats: Stats) {
        //should be handled by the block
    }
}