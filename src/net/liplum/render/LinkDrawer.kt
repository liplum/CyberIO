package net.liplum.render

import arc.Events
import mindustry.Vars
import mindustry.game.EventType
import mindustry.gen.Groups
import net.liplum.Settings
import net.liplum.api.cyber.IDataSender
import net.liplum.api.cyber.IStreamHost
import net.liplum.api.cyber.drawDataNetGraphic
import net.liplum.api.cyber.drawStreamGraphic

object LinkDrawer {
    @JvmStatic
    fun register() {
        Events.run(EventType.Trigger.postDraw) {
            draw()
        }
    }
    @JvmStatic
    fun draw() {
        if (!Settings.AlwaysShowLink) return
        val curTeam = Vars.player.team()
        Groups.build.each {
            if (it.team != curTeam) return@each
            if (it is IDataSender) {
                it.drawDataNetGraphic(false)
            } else if (it is IStreamHost) {
                it.drawStreamGraphic(false)
            }
        }
    }
}