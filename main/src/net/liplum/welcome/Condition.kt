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
) {
    init {
        this.register()
    }

    abstract fun canShow(tip: WelcomeScene): Boolean
    abstract fun priority(tip: WelcomeScene): Int

    companion object {
        val Default = object : Condition("Default") {
            override fun canShow(tip: WelcomeScene): Boolean {
                return false
            }

            override fun priority(tip: WelcomeScene) = Int.MIN_VALUE
        }
    }
}