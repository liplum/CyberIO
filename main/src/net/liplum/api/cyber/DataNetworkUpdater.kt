package net.liplum.api.cyber

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
}