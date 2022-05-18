package net.liplum

import net.liplum.lib.delegates.Delegate
import net.liplum.mdt.ClientOnly

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