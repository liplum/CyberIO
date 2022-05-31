package net.liplum.api.brain

import arc.graphics.Color
import arc.math.Mathf
import arc.scene.ui.layout.Scl
import arc.util.Align
import mindustry.Vars
import mindustry.gen.Building
import net.liplum.R
import net.liplum.lib.math.randomExcept
import net.liplum.lib.utils.BundleKey
import net.liplum.lib.utils.bundle
import net.liplum.lib.utils.inViewField
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.Text
import net.liplum.mdt.render.Toaster
import net.liplum.mdt.render.fadeInOutPct
import net.liplum.mdt.utils.inPayload

object ConversationManager {
    var ConversationFadeTimePercent = 0.08f
    var ConversationTime = 180f
    var fontSize = 0.9f
    @ClientOnly
    fun drawConversationOn(b: Building, text: String, color: Color) {
        Toaster.post(b.id, ConversationTime, overwrite = false) {
            Text.drawTextX {
                val curText = text.progressed(curTime / (ConversationTime * 0.4f))
                if (!b.inViewField(b.block.clipSize)) return@post
                setText(it, curText)
                it.data.setScale(1f / 4f / Scl.scl(fontSize))
                if (!b.isAdded && !b.inPayload) toast.duration = 0f
                it.color.set(color).a(fadeInOutPct(ConversationFadeTimePercent))
                it.draw(
                    curText,
                    b.x,
                    b.y + b.block.size * Vars.tilesize * 0.5f,
                    Align.center
                )
            }
        }
    }
    @ClientOnly
    fun hasConversationWith(b: Building) =
        Toaster[b.id] != null

    fun String.progressed(progress: Float): String {
        val p = progress.coerceIn(0f, 1f)
        val resLen = (length * p).toInt().coerceIn(0, length)
        return if (resLen < this.length)
            this.substring(0, resLen) + "."
        else
            this
    }
}

class Trigger(
    val id: String,
    var variance: Int
) {
    val bundleKeyPrefix = "$prefix.$id-"
    var color: Color = R.C.BrainWave

    companion object {
        // @formatter:off
        var killing =           Trigger("killing",              4)
        var control =           Trigger("control",              9)
        var controlInMod =      Trigger("control-in-mod",       3)
        var detect =            Trigger("detect",               3)
        /**
         * Detect enemy when heimdall is controlled by player
         */
        var detectControlled=   Trigger("detect-controlled",    4)
        var controlled =        Trigger("controlled",           8)
        var earKilling =        Trigger("ear-killing",          2)
        var earKillingFlying =  Trigger("ear-killing-flying",   2)
        var eyeDetect =         Trigger("eye-detect",           4)
        var hit =               Trigger("hit",                  6)
            .color(R.C.RedAlert)
        var partDestroyed =     Trigger("part-destroyed",       8)
            .color(R.C.RedAlert)
        var connect =           Trigger("connect",              6)
        var forceFieldHit =     Trigger("forcefield-hit",       3)
        var heal =              Trigger("heal",                 6)
            .color(R.C.GreenSafe)
        var onPayload =         Trigger("on-payload",           5)
        var onPayloadUnit =     Trigger("on-payload-unit",      5)
            .color(R.C.RedAlert)
        // @formatter:on
        private const val prefix = "heimdall.msg"
        private var lastNumber = 0
    }

    fun trigger(
        b: Building
    ) {
        ClientOnly {
            if (!ConversationManager.hasConversationWith(b)) {
                if (b.inViewField(b.block.clipSize)) {
                    val key = randomOne()
                    ConversationManager.drawConversationOn(
                        b, text = key.bundle,
                        color
                    )
                }
            }
        }
    }

    fun randomOne(): BundleKey {
        var selected = variance.randomExcept(lastNumber)
        if (selected == -1)
            selected = Mathf.random(0, variance - 1)
        lastNumber = selected
        return bundleKeyPrefix + selected
    }
}

fun Trigger.color(c: Color): Trigger {
    this.color = c
    return this
}