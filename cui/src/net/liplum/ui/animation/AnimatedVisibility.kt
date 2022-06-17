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
    fun update(e: Element) {
        curTime = if (isVisible)
            (curTime + Time.delta).coerceAtMost(duration)
        else
            (curTime - Time.delta).coerceAtLeast(0f)
        val progress = curTime / duration
        e.color.a(spec.decorate(progress))
    }
    /**
     * @exception BindingException throw if this has been bound.
     */
    fun bind(e: Element) {
        if (!bound) {
            e.update {
                update(e)
            }
            bound = true
        } else throw BindingException("This has already been bound, can't bind with $e.")
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
fun Element.bind(animatedVisibility: AnimatedVisibility): AnimatedVisibility {
    animatedVisibility.bind(this)
    return animatedVisibility
}

fun Element.AnimatedVisibility(
    isVisible: Boolean = true,
    duration: Float = 60f,
    spec: AnimationSpec = SmoothAnimationSpec(),
): AnimatedVisibility {
    val animatedVisibility = net.liplum.ui.animation.AnimatedVisibility(isVisible, duration, spec)
    this.bind(animatedVisibility)
    return animatedVisibility
}

