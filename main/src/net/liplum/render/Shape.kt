package net.liplum.render

import net.liplum.ResourceLoader
import net.liplum.annotations.SubscribeEvent
import net.liplum.event.CioLoadContentEvent
import net.liplum.cioTR
import plumy.core.assets.TR

object Shape {
    @JvmField var motionCircle = TR()
    @JvmField var starActive = TR()
    @JvmField var starInactive = TR()
    @JvmField var snow = TR()
    @SubscribeEvent(CioLoadContentEvent::class)
    fun load() {
        ResourceLoader += {
            motionCircle.set("shape-motion-circle".cioTR)
            starActive.set("star".cioTR)
            starInactive.set("star-inactive".cioTR)
            snow.set("shape-snow".cioTR)
        }
    }
}