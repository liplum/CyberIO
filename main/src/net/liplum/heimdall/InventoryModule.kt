package net.liplum.heimdall

import arc.scene.ui.layout.Table
import net.liplum.common.utils.IBundlable

class InventoryModule : IBundlable {
    lateinit var op: HeimdallOp
    fun build(t: Table) {

    }

    override val bundlePrefix = "inventory"
    override val parentBundle = HeimdallProjectGame
}