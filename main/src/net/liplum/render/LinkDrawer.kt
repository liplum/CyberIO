package net.liplum.render

import arc.graphics.g2d.Draw
import arc.util.Time
import mindustry.Vars
import mindustry.game.EventType
import net.liplum.Settings
import net.liplum.Var
import net.liplum.annotations.Subscribe
import net.liplum.api.ICyberEntity
import net.liplum.api.cyber.*
import plumy.core.math.smooth
import plumy.core.ClientOnly
import net.liplum.input.Inspector

object LinkDrawer {
    var lastSelected: ICyberEntity? = null
    var selectedTime = 0f
        set(value) {
            field = value.coerceIn(0f, Var.LinkDrawerTime)
        }
    @JvmStatic
    @ClientOnly
    @Subscribe(EventType.Trigger.drawOver)
    fun draw() {
        val selected = Inspector.curSelected
        if (selected is IDataSender || selected is IStreamHost || selected is IP2pNode) {
            lastSelected = selected as ICyberEntity
            selectedTime += Time.delta
        } else {
            selectedTime -= Time.delta
        }
        val unselectAlpha = 1f - (selectedTime / Var.LinkDrawerTime).smooth
        if (Settings.AlwaysShowLink) {
            if (unselectAlpha > 0f) {
                Var.GlobalLinkDrawerAlpha = unselectAlpha
                Vars.player.team().data().buildings.forEach {
                    var showCircle = Settings.ShowLinkCircle
                    if (it == lastSelected) {
                        Var.GlobalLinkDrawerAlpha = 1f
                        if (it == Inspector.curSelected)
                            showCircle = true
                    }
                    it.drawLink(showCircle)
                    Var.GlobalLinkDrawerAlpha = unselectAlpha
                }
            } else {
                Var.GlobalLinkDrawerAlpha = 1f
                selected?.drawLink(true)
            }
        } else {
            Var.GlobalLinkDrawerAlpha = 1f
            selected?.drawLink(true)
        }
        Var.GlobalLinkDrawerAlpha = 1f
        Draw.reset()
    }

    fun Any.drawLink(showCircle: Boolean) {
        when (this) {
            is IDataSender -> drawDataNetGraph(showCircle)
            is IStreamHost -> drawStreamGraph(showCircle)
            is IP2pNode -> drawP2PConnection(showCircle)
        }
    }
}