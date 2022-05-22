package net.liplum.blocks.ddos

import arc.scene.ui.layout.Table
import mindustry.Vars
import mindustry.ui.ItemImage
import mindustry.world.blocks.defense.turrets.Turret
import net.liplum.DebugOnly
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.ItemTypeAmount

class DDoS(name: String) : Turret(name) {
    @DebugOnly @ClientOnly var itemRow = 5

    inner class DDoSBuild : TurretBuild() {
        var alreadyUsed: IntArray = IntArray(ItemTypeAmount())
        override fun display(table: Table) {
            super.display(table)
            for ((i, item) in Vars.content.items().withIndex()) {
                table.add(ItemImage(item.uiIcon, alreadyUsed[i]))
                if (i % itemRow == 0) {
                    table.row()
                }
            }
        }
    }
}