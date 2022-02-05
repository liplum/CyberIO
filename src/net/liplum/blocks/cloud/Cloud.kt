package net.liplum.blocks.cloud

import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.modules.ItemModule
import net.liplum.animations.anims.IAnimated
import net.liplum.ui.bars.removeIfExist
import net.liplum.utils.autoAnim
import kotlin.experimental.and

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
        lateinit var cloudRoom: SharedRoom
        override fun create(block: Block, team: Team): Building {
            super.create(block, team)
            cloudRoom = LiplumCloud.getCloud(team)
            items = items ?: ItemModule()
            cloudRoom.online(this)
            return this
        }

        override fun writeBase(write: Writes?) {
            super.writeBase(write)
        }

        override var sharedItems: ItemModule
            get() = items
            set(value) {
                items = value
            }
    }
}