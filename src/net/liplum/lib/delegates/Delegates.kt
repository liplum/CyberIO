package net.liplum.lib.delegates

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
        arg8: Arg8
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

typealias DelegateHandler9<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9) -> Unit

class Delegate9<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9> {
    private val subscribers: HashSet<DelegateHandler9<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9>> = HashSet()
    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9
    ) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)
    }

    fun add(handler: DelegateHandler9<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9>): Delegate9<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler9<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9>): Delegate9<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate9<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler9<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler9<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9>) {
        subscribers.add(handler)
    }
}

typealias DelegateHandler10<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10) -> Unit

class Delegate10<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10> {
    private val subscribers: HashSet<DelegateHandler10<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10>> =
        HashSet()

    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9,
        arg10: Arg10
    ) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10)
    }

    fun add(handler: DelegateHandler10<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10>): Delegate10<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler10<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10>): Delegate10<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate10<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler10<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler10<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10>) {
        subscribers.add(handler)
    }
}

typealias DelegateHandler11<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11) -> Unit

class Delegate11<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11> {
    private val subscribers: HashSet<DelegateHandler11<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11>> =
        HashSet()

    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9,
        arg10: Arg10,
        arg11: Arg11
    ) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11)
    }

    fun add(handler: DelegateHandler11<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11>): Delegate11<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler11<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11>): Delegate11<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate11<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler11<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler11<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11>) {
        subscribers.add(handler)
    }
}

typealias DelegateHandler12<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12) -> Unit

class Delegate12<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12> {
    private val subscribers: HashSet<DelegateHandler12<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12>> =
        HashSet()

    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9,
        arg10: Arg10,
        arg11: Arg11,
        arg12: Arg12
    ) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12)
    }

    fun add(handler: DelegateHandler12<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12>): Delegate12<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler12<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12>): Delegate12<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate12<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler12<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler12<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12>) {
        subscribers.add(handler)
    }
}

typealias DelegateHandler13<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13) -> Unit

class Delegate13<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13> {
    private val subscribers: HashSet<DelegateHandler13<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13>> =
        HashSet()

    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9,
        arg10: Arg10,
        arg11: Arg11,
        arg12: Arg12,
        arg13: Arg13
    ) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13)
    }

    fun add(handler: DelegateHandler13<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13>): Delegate13<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler13<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13>): Delegate13<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate13<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler13<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler13<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13>) {
        subscribers.add(handler)
    }
}

typealias DelegateHandler14<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14) -> Unit

class Delegate14<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14> {
    private val subscribers: HashSet<DelegateHandler14<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14>> =
        HashSet()

    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9,
        arg10: Arg10,
        arg11: Arg11,
        arg12: Arg12,
        arg13: Arg13,
        arg14: Arg14
    ) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14)
    }

    fun add(handler: DelegateHandler14<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14>): Delegate14<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler14<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14>): Delegate14<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate14<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler14<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler14<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14>) {
        subscribers.add(handler)
    }
}

typealias DelegateHandler15<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15) -> Unit

class Delegate15<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15> {
    private val subscribers: HashSet<DelegateHandler15<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15>> =
        HashSet()

    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9,
        arg10: Arg10,
        arg11: Arg11,
        arg12: Arg12,
        arg13: Arg13,
        arg14: Arg14,
        arg15: Arg15
    ) {
        for (handler in subscribers)
            handler(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15)
    }

    fun add(handler: DelegateHandler15<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15>): Delegate15<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler15<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15>): Delegate15<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate15<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler15<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler15<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15>) {
        subscribers.add(handler)
    }
}

typealias DelegateHandler16<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16) -> Unit

class Delegate16<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16> {
    private val subscribers: HashSet<DelegateHandler16<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16>> =
        HashSet()

    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9,
        arg10: Arg10,
        arg11: Arg11,
        arg12: Arg12,
        arg13: Arg13,
        arg14: Arg14,
        arg15: Arg15,
        arg16: Arg16
    ) {
        for (handler in subscribers)
            handler(
                arg1,
                arg2,
                arg3,
                arg4,
                arg5,
                arg6,
                arg7,
                arg8,
                arg9,
                arg10,
                arg11,
                arg12,
                arg13,
                arg14,
                arg15,
                arg16
            )
    }

    fun add(handler: DelegateHandler16<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16>): Delegate16<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler16<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16>): Delegate16<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate16<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler16<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler16<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16>) {
        subscribers.add(handler)
    }
}

typealias DelegateHandler17<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17) -> Unit

class Delegate17<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17> {
    private val subscribers: HashSet<DelegateHandler17<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17>> =
        HashSet()

    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9,
        arg10: Arg10,
        arg11: Arg11,
        arg12: Arg12,
        arg13: Arg13,
        arg14: Arg14,
        arg15: Arg15,
        arg16: Arg16,
        arg17: Arg17
    ) {
        for (handler in subscribers)
            handler(
                arg1,
                arg2,
                arg3,
                arg4,
                arg5,
                arg6,
                arg7,
                arg8,
                arg9,
                arg10,
                arg11,
                arg12,
                arg13,
                arg14,
                arg15,
                arg16,
                arg17
            )
    }

    fun add(handler: DelegateHandler17<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17>): Delegate17<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler17<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17>): Delegate17<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate17<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler17<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler17<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17>) {
        subscribers.add(handler)
    }
}

typealias DelegateHandler18<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18) -> Unit

class Delegate18<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18> {
    private val subscribers: HashSet<DelegateHandler18<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18>> =
        HashSet()

    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9,
        arg10: Arg10,
        arg11: Arg11,
        arg12: Arg12,
        arg13: Arg13,
        arg14: Arg14,
        arg15: Arg15,
        arg16: Arg16,
        arg17: Arg17,
        arg18: Arg18
    ) {
        for (handler in subscribers)
            handler(
                arg1,
                arg2,
                arg3,
                arg4,
                arg5,
                arg6,
                arg7,
                arg8,
                arg9,
                arg10,
                arg11,
                arg12,
                arg13,
                arg14,
                arg15,
                arg16,
                arg17,
                arg18
            )
    }

    fun add(handler: DelegateHandler18<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18>): Delegate18<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler18<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18>): Delegate18<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate18<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler18<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler18<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18>) {
        subscribers.add(handler)
    }
}

typealias DelegateHandler19<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19> = (Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19) -> Unit

class Delegate19<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19> {
    private val subscribers: HashSet<DelegateHandler19<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19>> =
        HashSet()

    operator fun invoke(
        arg1: Arg1,
        arg2: Arg2,
        arg3: Arg3,
        arg4: Arg4,
        arg5: Arg5,
        arg6: Arg6,
        arg7: Arg7,
        arg8: Arg8,
        arg9: Arg9,
        arg10: Arg10,
        arg11: Arg11,
        arg12: Arg12,
        arg13: Arg13,
        arg14: Arg14,
        arg15: Arg15,
        arg16: Arg16,
        arg17: Arg17,
        arg18: Arg18,
        arg19: Arg19
    ) {
        for (handler in subscribers)
            handler(
                arg1,
                arg2,
                arg3,
                arg4,
                arg5,
                arg6,
                arg7,
                arg8,
                arg9,
                arg10,
                arg11,
                arg12,
                arg13,
                arg14,
                arg15,
                arg16,
                arg17,
                arg18,
                arg19
            )
    }

    fun add(handler: DelegateHandler19<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19>): Delegate19<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19> {
        subscribers.add(handler)
        return this
    }

    fun remove(handler: DelegateHandler19<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19>): Delegate19<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19> {
        subscribers.remove(handler)
        return this
    }

    fun clear(): Delegate19<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19> {
        subscribers.clear()
        return this
    }

    operator fun minusAssign(handler: DelegateHandler19<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19>) {
        subscribers.remove(handler)
    }

    operator fun plusAssign(handler: DelegateHandler19<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, Arg7, Arg8, Arg9, Arg10, Arg11, Arg12, Arg13, Arg14, Arg15, Arg16, Arg17, Arg18, Arg19>) {
        subscribers.add(handler)
    }
}

