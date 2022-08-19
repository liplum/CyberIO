package net.liplum

import net.liplum.common.delegate.Delegate
import plumy.core.ClientOnly

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

    operator fun minusAssign(task: () -> Unit) {
        ClientOnly {
            loadingTask -= task
        }
    }

    fun <T> T.onLoaded(task: T.() -> Unit): T {
        ClientOnly {
            loadingTask += { task() }
        }
        return this
    }
}