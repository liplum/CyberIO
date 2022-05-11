package net.liplum.render

import net.liplum.ClientOnly
import net.liplum.DebugOnly

@ClientOnly
@DebugOnly
object TestShader {
    @JvmStatic
    fun load() {
        /*
        val bufferScreen = FrameBuffer(Core.graphics.width, Core.graphics.height)
        Events.run(Trigger.draw) {
            bufferScreen.begin()
        }
        Events.run(Trigger.postDraw) {
            bufferScreen.end()
            Blending.disabled.apply()
            bufferScreen.blit(SD.TestScreen)
        }*/
    }
}