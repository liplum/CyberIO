package net.liplum.welcome

import net.liplum.welcome.ConditionRegistry.register

object ConditionRegistry {
    val conditions: MutableMap<String, Condition> = HashMap()
    operator fun get(id: String) =
        conditions[id] ?: Condition.Default

    operator fun get(id: Any?) =
        (id as? String)?.let { conditions[it] } ?: Condition.Default

    operator fun set(id: String, condition: Condition) {
        conditions[id] = condition
    }

    fun <T : Condition> T.register(): T {
        this@ConditionRegistry[id] = this
        return this
    }
}

abstract class Condition(
    val id: String,
    val priority: Int
) {
    init {
        this.register()
    }

    abstract fun canShow(): Boolean
    abstract fun applyShow(entity: Welcome.Entity, matches: List<WelcomeTip>)

    companion object {
        val Default = object : Condition("Default", Int.MIN_VALUE) {
            override fun canShow(): Boolean {
                return false
            }

            override fun applyShow(entity: Welcome.Entity, matches: List<WelcomeTip>) {
                return
            }
        }
    }
}