package net.liplum.blocks.ddos

import arc.scene.ui.layout.Table
import arc.util.Time
import mindustry.Vars
import mindustry.content.Bullets
import mindustry.entities.bullet.BulletType
import mindustry.game.EventType
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.type.Item
import mindustry.ui.ItemImage
import mindustry.world.blocks.defense.turrets.Turret
import net.liplum.DebugOnly
import net.liplum.annotations.SubscribeEvent
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.ItemTypeAmount

class DDoS(name: String) : Turret(name) {
    @DebugOnly @ClientOnly var itemRow = 5
    var acceptTime = 120f
    var bulletType: BulletType = Bullets.placeholder

    inner class DDoSBuild : TurretBuild() {
        var alreadyUsed: IntArray = IntArray(ItemTypeAmount())
        var curIndex = 0
            set(value) {
                field = value % ItemTypeAmount()
            }
        var acceptCounter = 0f
        override fun updateTile() {
            acceptCounter += Time.delta
            if (acceptCounter > acceptTime) {
                acceptCounter -= acceptTime
                curIndex++
            }
            super.updateTile()
        }

        override fun cheating() = true
        val curAcceptItem: Item
            get() = Vars.content.items()[curIndex]

        override fun acceptItem(source: Building, item: Item): Boolean {
            return item == curAcceptItem && items[item] < itemCapacity
        }

        override fun peekAmmo(): BulletType {
            return bulletType
        }

        override fun hasAmmo(): Boolean {
            return true
        }

        override fun handleBullet(bullet: Bullet, offsetX: Float, offsetY: Float, angleOffset: Float) {
            bullet.fdata = curAcceptItem.id.toFloat()
        }

        override fun display(table: Table) {
            super.display(table)
            table.row()
            for (i in alreadyUsed.indices) {
                alreadyUsed[i]++
            }
            table.add(Table().apply {
                left()
                for ((i, item) in Vars.content.items().withIndex()) {
                    add(ItemImage(item.uiIcon, alreadyUsed[i]))
                    if ((i + 1) % itemRow == 0) {
                        row()
                    }
                }
            })
        }
    }

    companion object {
        var skipTable = IntArray(0)
        @SubscribeEvent(EventType.WorldLoadEvent::class)
        fun updateSkipTable() {
            skipTable = IntArray(ItemTypeAmount())

        }
    }
}