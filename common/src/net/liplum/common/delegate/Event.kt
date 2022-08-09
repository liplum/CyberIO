package net.liplum.common.delegate

typealias EventHandler<T> = (sender: T, e: Args) -> Unit

class Event<TOwner : Any> {
    private val subscribers = HashSet<EventHandler<TOwner>>()
    operator fun invoke(sender: TOwner, e: Args) {
        for (handler in subscribers)
            handler(sender, e)
    }

    fun add(handler: EventHandler<TOwner>): Event<TOwner> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: EventHandler<TOwner>): Event<TOwner> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Event<TOwner> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: EventHandler<TOwner>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: EventHandler<TOwner>) {
        subscribers.add(handler)
    }
}