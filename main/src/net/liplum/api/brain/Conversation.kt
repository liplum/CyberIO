package net.liplum.api.brain

import arc.graphics.Color
import arc.math.Mathf
import arc.scene.ui.layout.Scl
import arc.util.Align
import mindustry.Vars
import mindustry.gen.Building
import net.liplum.R
import net.liplum.common.util.inViewField
import net.liplum.math.randomExcept
import net.liplum.render.Text
import net.liplum.render.Toaster
import net.liplum.render.fadeInOutPct
import net.liplum.utils.inPayload
import plumy.core.ClientOnly
import plumy.dsl.BundleKey
import plumy.dsl.bundle

object ConversationManager {
    var ConversationFadeTimePercent = 0.08f
    var TimePerCharacter = 10f
    var ConversationMinimumTime = 80f
    var fontSize = 0.9f
    private fun String.textToDuration() = TimePerCharacter * length
    @ClientOnly
    fun drawConversationOn(b: Building, text: String, color: Color) {
        val conversationTime = text.textToDuration()
        Toaster.post(b.id, conversationTime.coerceAtLeast(ConversationMinimumTime), overwrite = false) {
            Text.drawTextX {
                val curText = text.progressed(curTime / (conversationTime * 0.4f))
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

data class TriggerEntry(
    var key: BundleKey,
    var color: Color,
)

private val sharedEntry = TriggerEntry("", Color.white)

interface ITrigger {
    fun genEntry(): TriggerEntry
}

interface IVarianceTrigger : ITrigger {
    val variance: Int
    /**
     * @param variance starts with 0
     */
    fun mapTriggerEntry(variance: Int): TriggerEntry
}

open class Trigger(
    val id: String,
    override val variance: Int,
) : IVarianceTrigger {
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
        var tap =               Trigger("tap",                  7)
            .color(R.C.RedAlert)
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
        const val prefix = "heimdall.msg"
        var lastNumber = 0
    }

    override fun mapTriggerEntry(variance: Int): TriggerEntry = sharedEntry.apply {
        key = "$id-$variance"
        color = this@Trigger.color
    }

    override fun genEntry(): TriggerEntry {
        var selected = variance.randomExcept(lastNumber)
        if (selected == -1)
            selected = Mathf.random(0, variance - 1)
        lastNumber = selected
        return mapTriggerEntry(selected)
    }
}

fun ITrigger.trigger(
    b: Building,
) {
    ClientOnly {
        if (!ConversationManager.hasConversationWith(b)) {
            if (b.inViewField(b.block.clipSize)) {
                val entry = genEntry()
                ConversationManager.drawConversationOn(
                    b,
                    text = "${Trigger.prefix}.${entry.key}".bundle,
                    color = entry.color
                )
            }
        }
    }
}
// TODO: finish this
class MultiTrigger(
    vararg val triggers: Trigger,
) : IVarianceTrigger {
    val buckets = IntArray(triggers.size)
    override val variance: Int

    init {
        var sum = 0
        for (i in buckets.indices) {
            sum += triggers[i].variance
            buckets[i] = sum
        }
        variance = sum
    }

    override fun mapTriggerEntry(variance: Int): TriggerEntry {
        var index = 0
        var curSum = 0
        for (i in triggers.indices) {
            val sum = buckets[i]
            if (i < sum) {
                index = i
                curSum = sum
            }
        }
        val trigger = triggers[index]
        // now index is the end of variance
        return sharedEntry.apply {
            key = trigger.id + (variance - curSum)
            color = trigger.color
        }
    }

    override fun genEntry(): TriggerEntry {
        var selected = variance.randomExcept(Trigger.lastNumber)
        if (selected == -1)
            selected = Mathf.random(0, variance - 1)
        return mapTriggerEntry(selected)
    }
}

fun Trigger.color(c: Color): Trigger {
    this.color = c
    return this
}