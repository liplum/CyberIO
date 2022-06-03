package net.liplum.api.cyber

import mindustry.gen.Groups
import net.liplum.CLog
import net.liplum.DebugLevel
import net.liplum.DebugOnly
import net.liplum.mdt.mixin.EntityMixin

class DataNetworkUpdater : EntityMixin() {
    var network: DataNetwork? = null
    override fun update() {
        network?.update()
    }

    companion object {
        @JvmStatic
        fun create() =
            DataNetworkUpdater()
    }

    override fun add() {
        if (!this.added) {
            Groups.all.add(this)
            this.added = true
            DebugOnly(DebugLevel.Inspector) {
                CLog.info("$this added")
            }
        }
    }

    override fun remove() {
        if (added) {
            Groups.all.remove(this)
            added = false
            DebugOnly(DebugLevel.Inspector) {
                CLog.info("$this removed")
            }
        }
    }

    override fun toString() = "DataNetworkUpdater#$id"
}