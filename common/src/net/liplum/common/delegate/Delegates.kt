package net.liplum.common.delegate

typealias DelegateHandler = () -> Unit

class Delegate {
    private val subscribers: HashSet<DelegateHandler> = HashSet()
    operator fun invoke() {
        for (handler in subscribers)
            handler()
    }

    fun add(handler: DelegateHandler): Delegate {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler): Delegate {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler) {
        subscribers.add(handler)
    }
}
typealias DelegateHandler1<Arg1> = (Arg1) -> Unit

class Delegate1<Arg1> {
    private val subscribers: HashSet<DelegateHandler1<Arg1>> = HashSet()
    operator fun invoke(arg1: Arg1) {
        for (handler in subscribers)
            handler(arg1)
    }

    fun add(handler: DelegateHandler1<Arg1>): Delegate1<Arg1> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler1<Arg1>): Delegate1<Arg1> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate1<Arg1> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler1<Arg1>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler1<Arg1>) {
        subscribers.add(handler)
    }
}
typealias DelegateHandler2<Arg1, Arg2> = (Arg1, Arg2) -> Unit

class Delegate2<Arg1, Arg2> {
    private val subscribers: HashSet<DelegateHandler2<Arg1, Arg2>> = HashSet()
    operator fun invoke(arg1: Arg1, arg2: Arg2) {
        for (handler in subscribers)
            handler(arg1, arg2)
    }

    fun add(handler: DelegateHandler2<Arg1, Arg2>): Delegate2<Arg1, Arg2> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler2<Arg1, Arg2>): Delegate2<Arg1, Arg2> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate2<Arg1, Arg2> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler2<Arg1, Arg2>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler2<Arg1, Arg2>) {
        subscribers.add(handler)
    }
}
typealias DelegateHandler3<Arg1, Arg2, Arg3> = (Arg1, Arg2, Arg3) -> Unit

class Delegate3<Arg1, Arg2, Arg3> {
    private val subscribers: HashSet<DelegateHandler3<Arg1, Arg2, Arg3>> = HashSet()
    operator fun invoke(arg1: Arg1, arg2: Arg2, arg3: Arg3) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3)
    }

    fun add(handler: DelegateHandler3<Arg1, Arg2, Arg3>): Delegate3<Arg1, Arg2, Arg3> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler3<Arg1, Arg2, Arg3>): Delegate3<Arg1, Arg2, Arg3> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate3<Arg1, Arg2, Arg3> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler3<Arg1, Arg2, Arg3>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler3<Arg1, Arg2, Arg3>) {
        subscribers.add(handler)
    }
}
typealias DelegateHandler4<Arg1, Arg2, Arg3, Arg4> = (Arg1, Arg2, Arg3, Arg4) -> Unit

class Delegate4<Arg1, Arg2, Arg3, Arg4> {
    private val subscribers: HashSet<DelegateHandler4<Arg1, Arg2, Arg3, Arg4>> = HashSet()
    operator fun invoke(arg1: Arg1, arg2: Arg2, arg3: Arg3, arg4: Arg4) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4)
    }

    fun add(handler: DelegateHandler4<Arg1, Arg2, Arg3, Arg4>): Delegate4<Arg1, Arg2, Arg3, Arg4> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler4<Arg1, Arg2, Arg3, Arg4>): Delegate4<Arg1, Arg2, Arg3, Arg4> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate4<Arg1, Arg2, Arg3, Arg4> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler4<Arg1, Arg2, Arg3, Arg4>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler4<Arg1, Arg2, Arg3, Arg4>) {
        subscribers.add(handler)
    }
}
typealias DelegateHandler5<Arg1, Arg2, Arg3, Arg4, Arg5> = (Arg1, Arg2, Arg3, Arg4, Arg5) -> Unit

class Delegate5<Arg1, Arg2, Arg3, Arg4, Arg5> {
    private val subscribers: HashSet<DelegateHandler5<Arg1, Arg2, Arg3, Arg4, Arg5>> = HashSet()
    operator fun invoke(arg1: Arg1, arg2: Arg2, arg3: Arg3, arg4: Arg4, arg5: Arg5) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5)
    }

    fun add(handler: DelegateHandler5<Arg1, Arg2, Arg3, Arg4, Arg5>): Delegate5<Arg1, Arg2, Arg3, Arg4, Arg5> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler5<Arg1, Arg2, Arg3, Arg4, Arg5>): Delegate5<Arg1, Arg2, Arg3, Arg4, Arg5> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate5<Arg1, Arg2, Arg3, Arg4, Arg5> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler5<Arg1, Arg2, Arg3, Arg4, Arg5>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler5<Arg1, Arg2, Arg3, Arg4, Arg5>) {
        subscribers.add(handler)
    }
}
typealias DelegateHandler6<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6) -> Unit

class Delegate6<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6> {
    private val subscribers: HashSet<DelegateHandler6<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6>> = HashSet()
    operator fun invoke(arg1: Arg1, arg2: Arg2, arg3: Arg3, arg4: Arg4, arg5: Arg5, arg6: Arg6) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5, arg6)
    }

    fun add(handler: DelegateHandler6<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6>): Delegate6<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler6<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6>): Delegate6<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate6<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler6<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler6<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6>) {
        subscribers.add(handler)
    }
}
typealias DelegateHandler7<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7) -> Unit

class Delegate7<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7> {
    private val subscribers: HashSet<DelegateHandler7<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7>> = HashSet()
    operator fun invoke(arg1: Arg1, arg2: Arg2, arg3: Arg3, arg4: Arg4, arg5: Arg5, arg6: Arg6, arg7: Arg7) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5, arg6, arg7)
    }

    fun add(handler: DelegateHandler7<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7>): Delegate7<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler7<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7>): Delegate7<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate7<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler7<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler7<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7>) {
        subscribers.add(handler)
    }
}
typealias DelegateHandler8<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8) -> Unit

class Delegate8<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8> {
    private val subscribers: HashSet<DelegateHandler8<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8>> = HashSet()
    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
    ) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8)
    }

    fun add(handler: DelegateHandler8<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8>): Delegate8<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler8<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8>): Delegate8<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate8<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler8<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler8<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8>) {
        subscribers.add(handler)
    }
}