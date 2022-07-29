package net.liplum.render

import mindustry.Vars
import mindustry.game.EventType
import net.liplum.Settings
import net.liplum.annotations.Subscribe
import net.liplum.api.cyber.*
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.advanced.Inspector

object LinkDrawer {
    @JvmStatic
    @ClientOnly
    @Subscribe(EventType.Trigger.drawOver)
    fun draw() {
        if (!Settings.AlwaysShowLink) return
        when (val selected = Inspector.curSelected) {
            is IDataReceiver -> {
                selected.drawDataNetGraph(showCircle = Settings.ShowLinkCircle)
            }
            is IStreamHost -> {
                selected.drawStreamGraph(showCircle = Settings.ShowLinkCircle)
            }
            is IP2pNode -> {
                selected.drawP2PConnection(showCircle = Settings.ShowLinkCircle)
            }
            else -> {
                Vars.player.team().data().buildings.forEach {
                    when (it) {
                        is IDataSender -> it.drawDataNetGraph(showCircle = Settings.ShowLinkCircle)
                        is IStreamHost -> it.drawStreamGraph(showCircle = Settings.ShowLinkCircle)
                        is IP2pNode -> it.drawP2PConnection(showCircle = Settings.ShowLinkCircle)
                    }
                }
            }
        }
    }
}