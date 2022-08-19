package net.liplum.render

import plumy.core.ClientOnly
import net.liplum.DebugOnly

@ClientOnly
@DebugOnly
object TestShader {
    @JvmStatic
    fun load() {
        /*//val bufferScreen = FrameBuffer(Core.graphics.width, Core.graphics.height)
        val bufferScreen = FrameBuffer(Core.graphics.width, Core.graphics.height)
        RunOnDraw {
            bufferScreen.begin()
        }
        RunOnPostDraw {
            bufferScreen.end()
            Blending.additive.apply()
            SD.TestScreen.useEffectBuffer = false
            bufferScreen.blit(SD.TestScreen)
            SD.TestScreen.useEffectBuffer = true
        }*/
    }
}