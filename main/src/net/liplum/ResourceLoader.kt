package net.liplum

import net.liplum.lib.delegates.Delegate

@ClientOnly
object ResourceLoader {
    private val loadingTask: Delegate = Delegate()
    @JvmStatic
    @ClientOnly
    fun loadAllResources() {
        loadingTask()
        loadingTask.clear()
    }

    operator fun plusAssign(task: () -> Unit) {
        loadingTask += task
    }

    operator fun minusAssign(handler: () -> Unit) {
        loadingTask -= handler
    }

}