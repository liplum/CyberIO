package net.liplum.blocks.cloud

import arc.graphics.g2d.Draw
import arc.math.Mathf
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.type.Item
import mindustry.world.Block
import mindustry.world.modules.ItemModule
import net.liplum.animations.anims.IAnimated
import net.liplum.ui.bars.removeIfExist
import net.liplum.utils.TR
import net.liplum.utils.subA

open class Cloud(name: String) : Block(name) {
    lateinit var floatingCloudTR: IAnimated
    lateinit var cloud: TR

    init {
        solid = true
        update = true
        hasItems = true
        saveConfig = true
        configurable = true
        itemCapacity = 100
    }

    override fun setBars() {
        super.setBars()
        bars.removeIfExist("items")
    }

    override fun load() {
        super.load()
        cloud = this.subA("cloud")
    }
    //override fun outputsItems() = false
    open inner class CloudBuild : Building(), IShared {
        lateinit var cloudRoom: SharedRoom
        override fun create(block: Block, team: Team): Building {
            super.create(block, team)
            cloudRoom = LiplumCloud.getCloud(team)
            cloudRoom.online(this)
            return this
        }

        override fun remove() {
            super.remove()
            cloudRoom.offline(this)
        }

        override fun acceptItem(source: Building, item: Item) = true
        override fun getSharedItems(): ItemModule = items
        override fun setSharedItems(itemModule: ItemModule) {
            items = itemModule
        }

        override fun getBuilding() = this
        override fun draw() {
            super.draw()
            val floatRange = 1f
            Draw.rect(
                cloud,
                x + Mathf.random(-floatRange, floatRange),
                y + Mathf.random(-floatRange, floatRange)
            )
        }
    }
}