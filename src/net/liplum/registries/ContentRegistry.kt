package net.liplum.registries

import java.util.*

object ContentRegistry {
    var ContentTables: MutableList<ContentTable> = LinkedList()
    var ItemList = CioItems().add()
    var StatusEffectList = CioStatusEffects().add()
    var BlockList = CioBlocks().add()
    var UnitTypeList = CioUnitTypes().add()
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
    }

    fun ContentTable.add(): ContentTable {
        ContentTables.add(this)
        return this
    }
}