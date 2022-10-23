package net.liplum.render

import arc.math.geom.Rect
import mindustry.game.EventType
import mindustry.gen.Groups
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.annotations.Only
import net.liplum.annotations.Subscribe
import net.liplum.common.util.inViewField
import plumy.core.ClientOnly
import net.liplum.mixin.PowerGraphc
import net.liplum.render.G
import net.liplum.render.Text

@ClientOnly
object DebugDrawer {
    private val hitBox = Rect()
    @JvmStatic
    @Subscribe(EventType.Trigger.drawOver, Only.debug)
    fun draw() {
        DebugOnly {
            Groups.build.each {
                if (!it.inViewField(it.block.clipSize)) return@each
                if (Var.ShowPowerGraphID) {
                    it.power?.apply {
                        Text.drawTextEasy(
                            graph.id.toString(),
                            it.x, it.y, R.C.Holo
                        )
                    }
                }
                if (Var.DrawBuildCollisionRect) {
                    it.hitbox(hitBox)
                    G.rect(hitBox, stroke = 0.5f)
                }
            }
            Groups.unit.each {
                if (!it.inViewField(it.clipSize())) return@each
                if (Var.ShowPowerGraphID) {
                    (it as? PowerGraphc)?.payloadPower?.apply {
                        Text.drawTextEasy(
                            id.toString(),
                            it.x, it.y, R.C.GreenSafe
                        )
                    }
                }
                if (Var.DrawUnitCollisionRect) {
                    it.hitbox(hitBox)
                    G.rect(hitBox, stroke = 0.5f)
                }
            }
        }
    }
}