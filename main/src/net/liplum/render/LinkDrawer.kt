package net.liplum.render

import mindustry.Vars
import mindustry.game.EventType
import mindustry.gen.Groups
import net.liplum.Settings
import net.liplum.annotations.Subscribe
import net.liplum.api.cyber.IDataSender
import net.liplum.api.cyber.IStreamHost
import net.liplum.api.cyber.drawDataNetGraphic
import net.liplum.api.cyber.drawStreamGraphic
import net.liplum.mdt.ClientOnly

object LinkDrawer {
    @JvmStatic
    @Subscribe(EventType.Trigger.drawOver)
    @ClientOnly
    fun draw() {
        if (!Settings.AlwaysShowLink) return
        val curTeam = Vars.player.team()
        Groups.build.each {
            if (it.team != curTeam) return@each
            if (it is IDataSender) {
                it.drawDataNetGraphic(showCircle = Settings.ShowLinkCircle)
            } else if (it is IStreamHost) {
                it.drawStreamGraphic(showCircle = Settings.ShowLinkCircle)
            }
        }
    }
}