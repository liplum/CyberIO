package net.liplum.mdt.render

import arc.math.Interp
import arc.util.Time
import mindustry.game.EventType
import net.liplum.annotations.Subscribe
import net.liplum.lib.utils.invoke
import net.liplum.mdt.ClientOnly

object Toaster {
    /**
     * ToastSpec can be reused.
     */
    private val shared = ToastSpec()
    /**
     * Unmanaged toast is impossible to be removed.
     */
    val unmanagedToasts = HashSet<Toast>()
    /**
     * Managed toast can be overwritten or removed by its key.
     */
    val managedToast = HashMap<Any, Toast>()
    /**
     * Post an unmanaged toast, it will be drawn every [EventType.Trigger.drawOver].
     * It is impossible to be removed.
     * This only works on Client Side without unnecessary object instantiation.
     */
    fun post(
        duration: Float,
        useGlobalTime: Boolean = false,
        task: ToastSpec.() -> Unit
    ) {
        ClientOnly {
            val toast = genToast(duration, useGlobalTime, task)
            unmanagedToasts.add(toast)
        }
    }
    /**
     * Post an unmanaged toast, it will be drawn every [EventType.Trigger.drawOver].
     * It is impossible to be removed.
     * When this is used on Server Side, it'll at least create an [Toast].
     */
    fun add(
        duration: Float,
        useGlobalTime: Boolean = false,
        task: ToastSpec.() -> Unit
    ): Toast {
        val toast = genToast(duration, useGlobalTime, task)
        ClientOnly {
            unmanagedToasts.add(toast)
        }
        return toast
    }
    /**
     * Post a managed toast, it will be drawn every [EventType.Trigger.drawOver].
     * It can be overwritten or removed by its key.
     * This only works on Client Side without unnecessary object instantiation.
     */
    fun post(
        id: Any,
        duration: Float,
        useGlobalTime: Boolean = false,
        overwrite: Boolean = true,
        task: ToastSpec.() -> Unit
    ) {
        ClientOnly {
            if (overwrite) {
                val toast = genToast(duration, useGlobalTime, task)
                managedToast[id] = toast
            } else {
                val former = managedToast[id]
                if (former == null) {
                    val toast = genToast(duration, useGlobalTime, task)
                    managedToast[id] = toast
                }
            }
        }
    }
    /**
     * Post a managed toast, it will be drawn every [EventType.Trigger.drawOver].
     * It can be overwritten or removed by its key.
     * When this is used on Server Side, it'll at least create an [Toast].
     */
    fun add(
        id: Any,
        duration: Float,
        useGlobalTime: Boolean = false,
        overwrite: Boolean = true,
        task: ToastSpec.() -> Unit
    ): Toast {
        return if (overwrite) {
            val toast = genToast(duration, useGlobalTime, task)
            ClientOnly {
                managedToast[id] = toast
            }
            toast
        } else {
            val former = managedToast[id]
            if (former == null) {
                val toast = genToast(duration, useGlobalTime, task)
                ClientOnly {
                    managedToast[id] = toast
                }
                toast
            } else {
                former
            }
        }
    }

    private fun genToast(
        duration: Float,
        useGlobalTime: Boolean = false,
        task: ToastSpec.() -> Unit
    ) = if (useGlobalTime)
        Toast(Time.globalTime, duration, true, task)
    else
        Toast(Time.time, duration, false, task)
    /**
     * Remove a managed toast.
     */
    @ClientOnly
    fun remove(id: Any): Toast? {
        return managedToast.remove(id)
    }
    /**
     * Get a managed toast or null.
     */
    @ClientOnly
    operator fun get(id: Any): Toast? {
        return managedToast[id]
    }
    /**
     * This should be called after drawing
     */
    @ClientOnly
    @Subscribe(EventType.Trigger.drawOver)
    fun drawAllToasts() {
        // Draw all unmanaged toasts
        run {
            val it = unmanagedToasts.iterator()
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
        // Draw all managed toasts
        run {
            val it = managedToast.iterator()
            while (it.hasNext()) {
                val toast = it.next().value
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
    /**
     * Clear all toasts.
     */
    @ClientOnly
    fun clear() {
        unmanagedToasts.clear()
        managedToast.clear()
    }
}

class Toast(
    var startTime: Float,
    var duration: Float,
    var useGlobalTime: Boolean,
    var task: ToastSpec.() -> Unit
) {
    val isEnd: Boolean
        get() = if (useGlobalTime) startTime + duration <= Time.globalTime
        else startTime + duration <= Time.time

    companion object {
        val X = Toast(0f, 0f, false) {}
    }
}

class ToastSpec {
    var toast = Toast.X
    var curTime = 0f
    val fin: Float
        get() = curTime / toast.duration
    val fout: Float
        get() = 1 - curTime / toast.duration
}
/**
 * Use the default fade-in effect from [Interp.fade]
 */
val ToastSpec.fadeIn: Float
    get() = Interp.fade(fin)
/**
 * Use the default fade-out effect from [Interp.fade]
 */
val ToastSpec.fadeOut: Float
    get() = Interp.fade(fout)
/**
 * @param duration the duration of fade-in&out
 */
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
/**
 * @param durationPct final fade-in&out duration = toast.duration * durationPct
 */
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