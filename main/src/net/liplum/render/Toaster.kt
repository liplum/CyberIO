package net.liplum.render

import arc.Events
import arc.math.Interp
import arc.util.Time
import mindustry.game.EventType
import net.liplum.ClientOnly
import net.liplum.annotations.Subscribe
import net.liplum.utils.invoke

@ClientOnly
object Toaster {
    /**
     * ToastSpec can be reused.
     */
    @ClientOnly
    private val shared = ToastSpec()
    val allToasts = HashSet<Toast>()
    /**
     * Post a toast, it will be drawn every [EventType.Trigger.drawOver]
     */
    @ClientOnly
    fun post(
        duration: Float,
        useGlobalTime: Boolean = false,
        task: ToastSpec.() -> Unit
    ) {
        val toast = if (useGlobalTime)
            Toast(Time.globalTime, duration, true, task)
        else
            Toast(Time.time, duration, false, task)
        allToasts.add(toast)
    }
    @ClientOnly
    @Subscribe(EventType.Trigger.drawOver)
    fun drawAllToast() {
        val it = allToasts.iterator()
        while (it.hasNext()) {
            val toast = it.next()
            val curClock = if (toast.useGlobalTime) Time.globalTime else Time.time
            val curTime = curClock - toast.startTime
            if (curTime >= toast.duration) {
                it.remove()
            } else {
                shared.toast = toast
                shared.curTime = curTime
                shared.toast.task(shared)
            }
        }
    }
}
@ClientOnly
class Toast(
    val startTime: Float,
    val duration: Float,
    val useGlobalTime: Boolean,
    val task: ToastSpec.() -> Unit
) {
    companion object {
        val X = Toast(0f, 0f, false) {}
    }
}
@ClientOnly
class ToastSpec {
    var toast = Toast.X
    var curTime = 0f
    val fin: Float
        get() = curTime / toast.duration
    val fout: Float
        get() = 1 - curTime / toast.duration
}

val ToastSpec.fadeIn: Float
    get() = Interp.fade(fin)
val ToastSpec.fadeOut: Float
    get() = Interp.fade(fout)

fun ToastSpec.fadeInOut(
    duration: Float = toast.duration * 0.1f
): Float =
    // Fade in:
    if (curTime < duration)
        Interp.fade(curTime / duration)
    // Fade out
    else if (toast.duration - curTime < duration)
        Interp.fade((toast.duration - curTime) / duration)
    // Appear clearly
    else
        1f

fun ToastSpec.fadeInOutPct(
    durationPct: Float = 0.1f
): Float {
    val duration = durationPct * toast.duration
    // Fade in:
    return if (curTime < duration)
        Interp.fade(curTime / duration)
    // Fade out
    else if (toast.duration - curTime < duration)
        Interp.fade((toast.duration - curTime) / duration)
    // Appear clearly
    else
        1f
}