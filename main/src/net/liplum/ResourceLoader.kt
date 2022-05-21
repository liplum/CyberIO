package net.liplum

import net.liplum.lib.delegates.Delegate
import net.liplum.mdt.ClientOnly

object ResourceLoader {
    private val loadingTask: Delegate = Delegate()
    @JvmStatic
    fun loadAllResources() {
        ClientOnly {
            loadingTask()
            loadingTask.clear()
        }
    }

    operator fun plusAssign(task: () -> Unit) {
        ClientOnly {
            loadingTask += task
        }
    }

    operator fun minusAssign(handler: () -> Unit) {
        ClientOnly {
            loadingTask -= handler
        }
    }
}