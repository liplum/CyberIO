package net.liplum.render

import mindustry.game.EventType
import mindustry.gen.Groups
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.annotations.Only
import net.liplum.annotations.Subscribe
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.mixin.PowerGraphc
import net.liplum.mdt.render.Text

@ClientOnly
object DebugDrawer {
    @JvmStatic
    @Subscribe(EventType.Trigger.drawOver, Only.debug)
    fun draw() {
        DebugOnly {
            if(!Var.ShowPowerGraphID) return
            Groups.build.each {
                DebugOnly {
                    val power = it.power
                    if (power != null) {
                        Text.drawTextEasy(
                            power.graph.id.toString(),
                            it.x, it.y, R.C.Holo
                        )
                    }
                }
            }
            Groups.unit.each {
                if (it is PowerGraphc) {
                    val payloadPower = it.payloadPower
                    if (payloadPower != null) {
                        Text.drawTextEasy(
                            payloadPower.id.toString(),
                            it.x, it.y, R.C.GreenSafe
                        )
                    }
                }
            }
        }
    }
}