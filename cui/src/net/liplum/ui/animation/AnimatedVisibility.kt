package net.liplum.ui.animation

import arc.scene.Element
import arc.util.Time
import net.liplum.ui.BindingException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AnimatedVisibility(
    var isVisible: Boolean = true,
    var duration: Float = 60f,
    var spec: AnimationSpec = SmoothAnimationSpec(),
) : ReadWriteProperty<Any?, Boolean> {
    var curTime = 0f
    var bound = false
    val isEnd: Boolean
        get() = if (isVisible) curTime >= duration else curTime <= 0f

    fun update(e: Element) {
        val progress = curTime / duration
        e.color.a(spec.decorate(progress))
    }

    fun restart() {
        curTime = if (isVisible) 0f else duration
    }

    fun updateTimer() {
        curTime = if (isVisible)
            (curTime + Time.delta).coerceAtMost(duration)
        else
            (curTime - Time.delta).coerceAtLeast(0f)
    }
    /**
     * @exception BindingException throw if this has been bound.
     */
    fun bindAll(e: Element) {
        if (!bound) {
            e.update {
                updateTimer()
                update(e)
            }
            bound = true
        } else throw BindingException("This has already been bound, can't bind with $e.")
    }
    fun bindTimer(e: Element) {
        if (!bound) {
            e.update {
                updateTimer()
            }
            bound = true
        }
    }

    fun bind(e: Element) {
        e.update {
            update(e)
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return isVisible
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        isVisible = value
    }
}
/**
 * @exception BindingException throw if this [animatedVisibility] has been bound.
 */
fun Element.bindAll(animatedVisibility: AnimatedVisibility): AnimatedVisibility {
    animatedVisibility.bindAll(this)
    return animatedVisibility
}

fun Element.bindTimer(animatedVisibility: AnimatedVisibility): AnimatedVisibility {
    animatedVisibility.bindTimer(this)
    return animatedVisibility
}

fun Element.bind(animatedVisibility: AnimatedVisibility): AnimatedVisibility {
    animatedVisibility.bind(this)
    return animatedVisibility
}

fun Element.animatedVisibility(
    isVisible: Boolean = true,
    duration: Float = 60f,
    spec: AnimationSpec = SmoothAnimationSpec(),
): AnimatedVisibility {
    val animatedVisibility = AnimatedVisibility(isVisible, duration, spec)
    this.bindAll(animatedVisibility)
    return animatedVisibility
}

