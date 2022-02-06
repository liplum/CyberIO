package net.liplum.blocks.cloud

import mindustry.game.Team
import mindustry.gen.Building
import mindustry.type.Item
import mindustry.world.Block
import mindustry.world.modules.ItemModule
import net.liplum.animations.anims.IAnimated
import net.liplum.ui.bars.removeIfExist
import net.liplum.utils.autoAnim

open class Cloud(name: String) : Block(name) {
    lateinit var floatingCloudTR: IAnimated

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
        floatingCloudTR = this.autoAnim("cloud", 7, 120f)
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
    }
}