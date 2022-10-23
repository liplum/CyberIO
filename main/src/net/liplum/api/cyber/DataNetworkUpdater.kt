package net.liplum.api.cyber

import mindustry.gen.Groups
import net.liplum.CLog
import net.liplum.DebugLevel
import net.liplum.DebugOnly
import net.liplum.mixin.EntityMixin

class DataNetworkUpdater : EntityMixin()
//    , SimpleSyncMixin
{
    var network: DataNetwork? = null
    //override var lastUpdate: Long = 0
    //override var updateSpacing: Long = 0
    override fun update() {
        network?.update()
    }

    override fun add() {
        if (!this.added) {
            Groups.all.add(this)
            //    Groups.sync.add(this)
            this.added = true
            DebugOnly(DebugLevel.Inspector) {
                CLog.info("$this added")
            }
        }
    }

    override fun remove() {
        if (added) {
            Groups.all.remove(this)
            //      Groups.sync.remove(this)
            added = false
            DebugOnly(DebugLevel.Inspector) {
                CLog.info("$this removed")
            }
        }
    }

    override fun toString() = "DataNetworkUpdater#$id"

    companion object {
        @JvmStatic
        fun create() =
            DataNetworkUpdater()
    }
}