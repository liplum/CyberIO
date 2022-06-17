package net.liplum.render

import net.liplum.ResourceLoader
import net.liplum.annotations.SubscribeEvent
import net.liplum.events.CioLoadContentEvent
import net.liplum.inCio
import net.liplum.lib.assets.TR

object Shapes {
    @JvmField var motionCircle = TR()
    @JvmField var starActive = TR()
    @JvmField var starInactive = TR()
    @JvmField var snow = TR()
    @SubscribeEvent(CioLoadContentEvent::class)
    fun load() {
        ResourceLoader += {
            motionCircle.set("shape-motion-circle".inCio)
            starActive.set("star".inCio)
            starInactive.set("star-inactive".inCio)
            snow.set("shape-snow".inCio)
        }
    }
}