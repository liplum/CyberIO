package net.liplum.heimdall

import arc.scene.ui.layout.Table
import net.liplum.lib.utils.Bundlable

class InventoryModule : Bundlable {
    lateinit var op: HeimdallOp
    fun build(t: Table) {

    }

    override val bundlePrefix = "inventory"
    override val parentBundle = HeimdallProjectGame
}