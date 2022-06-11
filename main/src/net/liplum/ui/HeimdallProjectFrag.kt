package net.liplum.ui

import arc.scene.ui.Image
import arc.scene.ui.layout.Table
import mindustry.gen.Tex
import net.liplum.inCio
import net.liplum.lib.ui.TRD
import net.liplum.lib.ui.UIToast
import net.liplum.lib.utils.Bundlable
import net.liplum.mdt.ClientOnly

@ClientOnly
object HeimdallProjectFrag : Bundlable {
    override val bundlePrefix = "heimdall"
    var toastUI = UIToast().apply {
        background = Tex.button
    }
    val title: String
        get() = bundle("title")

    fun build(cont: Table) {
        val heimdall = TRD("heimdall".inCio)
        cont.add(Image(heimdall))
    }
}