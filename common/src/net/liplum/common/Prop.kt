package net.liplum.common

typealias PropListener = () -> Unit

class Prop<V>(default: V) {
    private val subscribers: HashSet<PropListener> = HashSet()
    var value: V = default
        set(value) {
            if (field != value) {
                field = value
                notifyChange()
            }
        }

    private fun notifyChange() {
        for (handler in subscribers)
            handler()
    }

    fun add(listener: PropListener): Prop<V> {
        subscribers.add(listener)
        return this
    }

    fun remove(listener: PropListener): Prop<V> {
        subscribers.remove(listener)
        return this
    }

    fun clear(): Prop<V> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(listener: PropListener) {
        subscribers.remove(listener)
    }

    operator fun plusAssign(listener: PropListener) {
        subscribers.add(listener)
    }
}