package net.liplum.render

import mindustry.Vars
import mindustry.game.EventType
import net.liplum.Settings
import net.liplum.annotations.Subscribe
import net.liplum.api.cyber.*
import net.liplum.mdt.ClientOnly

object LinkDrawer {
    @JvmStatic
    @ClientOnly
    @Subscribe(EventType.Trigger.drawOver)
    fun draw() {
        if (!Settings.AlwaysShowLink) return
        Vars.player.team().data().buildings.each {
            when (it) {
                is IDataSender -> it.drawDataNetGraph(showCircle = Settings.ShowLinkCircle)
                is IStreamHost -> it.drawStreamGraph(showCircle = Settings.ShowLinkCircle)
                is IP2pNode -> it.drawP2PConnection(showCircle = Settings.ShowLinkCircle)
            }
        }
    }
}