package net.liplum.data

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