package net.liplum.common

import net.liplum.common.delegate.Delegate2
import net.liplum.common.delegate.DelegateHandler1
import net.liplum.common.delegate.DelegateHandler2

typealias ObserverAny = Observer<Any>
typealias ObserverAnyNull = ObserverNull<Any>

open class Observer<T>(
    val init: T,
    val getter: () -> T,
) {
    constructor(getter: () -> T) : this(getter(), getter)

    var last: T = init
    /**
     * para1: last value
     * para2: current value
     */
    val onChanged = Delegate2<T, T>()
    open fun update() {
        val present = getter()
        if (present != last) {
            onChanged(last, present)
            last = present
        }
    }

    open fun notify(func: DelegateHandler2<T, T>): Observer<T> {
        onChanged.add(func)
        return this
    }

    inline fun notify(crossinline func: DelegateHandler1<T>): Observer<T> {
        notify { _, cur ->
            func(cur)
        }
        return this
    }
}

open class ObserverNull<T>(
    val init: T?,
    val getter: () -> T?,
) {
    constructor(getter: () -> T?) : this(null, getter)

    var last: T? = init
    /**
     * para1: last value
     * para2: current value
     */
    val onChanged = Delegate2<T?, T?>()
    open fun update() {
        val present = getter()
        if (present != last) {
            onChanged(last, present)
            last = present
        }
    }

    open fun notify(func: DelegateHandler2<T?, T?>): ObserverNull<T> {
        onChanged.add(func)
        return this
    }

    inline fun notify(crossinline func: DelegateHandler1<T?>): ObserverNull<T> {
        notify { _, cur ->
            func(cur)
        }
        return this
    }
}
