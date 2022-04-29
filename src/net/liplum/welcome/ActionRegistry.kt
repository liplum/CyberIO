package net.liplum.welcome

object ActionRegistry {
    val actions: MutableMap<String, Action> = HashMap()
    operator fun get(id: String) =
        actions[id] ?: Action.Default

    operator fun get(id: Any?) =
        (id as? String)?.let { actions[it] } ?: Action.Default

    operator fun set(id: String, action: Action) {
        actions[id] = action
    }

    fun <T : Action> T.register(): T {
        this@ActionRegistry[id] = this
        return this
    }
}

abstract class Action(
    val id: String,
) {
    abstract fun doAction(entity: Welcome.Entity)
    operator fun invoke(entity: Welcome.Entity) {
        doAction(entity)
    }

    companion object {
        val Default = object : Action("Default") {
            override fun doAction(entity: Welcome.Entity) {
            }
        }
    }
}
