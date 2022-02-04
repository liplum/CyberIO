package net.liplum.blocks.cloud

import mindustry.game.Team
import mindustry.gen.Building
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
        configurable = true
    }

    override fun setBars() {
        super.setBars()
        bars.removeIfExist("items")
    }

    override fun load() {
        super.load()
        floatingCloudTR = this.autoAnim("cloud", 7, 120f)
    }

    override fun outputsItems() = false
    open inner class CloudBuild : Building(), IShared {
        lateinit var couldRoom: SharedRoom
        override fun create(block: Block, team: Team): Building {
            super.create(block, team)
            couldRoom = LiplumCloud.getCloud(team)
            items = items ?: ItemModule()
            return this
        }

        override var sharedItems: ItemModule
            get() = items
            set(value) {
                items = value
            }
    }
}