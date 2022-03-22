package net.liplum.lib

private typealias BCondition = Condition
private typealias ResultCondition = Condition

fun not(condition: Condition): Condition =
    condition.not()

open class Condition(
    val condition: () -> Boolean
) {
    val and: HashMap<BCondition, ResultCondition> = HashMap()
    val or: HashMap<BCondition, ResultCondition> = HashMap()
    var not: ResultCondition? = null
    operator fun plus(b: Condition): ResultCondition =
        this and b

    infix fun and(b: Condition): ResultCondition {
        val res = and[b]
        return if (res != null) {
            res
        } else {
            val newRes = Condition { condition() && b.condition() }
            and[b] = newRes
            b.and[this] = newRes
            newRes
        }
    }

    operator fun div(b: Condition): ResultCondition =
        this or b

    infix fun or(b: Condition): ResultCondition {
        val res = or[b]
        return if (res != null) {
            res
        } else {
            val newRes = Condition { condition() || b.condition() }
            or[b] = newRes
            b.or[this] = newRes
            newRes
        }
    }

    operator fun not(): ResultCondition {
        var not = not
        return if (not != null) {
            not
        } else {
            not = Condition { !condition() }
            this.not = not
            not
        }
    }

    inline operator fun invoke(func: () -> Unit): Boolean {
        if (condition()) {
            func()
            return true
        }
        return false
    }
}