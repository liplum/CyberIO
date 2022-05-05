package net.liplum.registries

import java.util.*

object ContentRegistry {
    var ContentTables: MutableList<ContentTable> = LinkedList()
    var ItemList = CioItems.add()
    var LiquidList = CioLiquids.add()
    var StatusEffectList = CioSEffects.add()
    var BulletsList = CioBulletTypes.add()
    var BlockList = CioBlocks.add()
    var UnitTypeList = CioUnitTypes.add()
    @JvmStatic
    fun loadContent() {
        ContentTables.forEach {
            it.firstLoad()
        }
        ContentTables.forEach {
            it.load()
        }
        ContentTables.forEach {
            it.lastLoad()
        }
        CioTechTree.loadSerpulo()
    }
    @JvmStatic
    fun ContentTable.add(): ContentTable {
        ContentTables.add(this)
        return this
    }
}