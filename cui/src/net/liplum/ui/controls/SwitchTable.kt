package net.liplum.ui.controls

import arc.scene.ui.layout.Table
import net.liplum.ui.animation.AnimatedVisibility
import java.util.*

open class SwitchTable(
    var animation: AnimatedVisibility,
) : Table() {
    var queue = LinkedList<Table>()
    var cur: Table? = null
    fun setContent(content: Table) {
        queue.add(content)
    }

    protected fun updateAnimation() {
        animation.updateTimer()
    }

    var curContent: Table? = null
    protected fun updateContent() {
        curContent?.let {
            animation.update(it)
        }
        if (queue.isNotEmpty() && animation.isVisible && animation.isEnd) {
            animation.isVisible = false
        }
        if (queue.isNotEmpty() && !animation.isVisible && animation.isEnd) {
            val next = queue.poll()
            curContent = next
            clear()
            add(next).grow()
            animation.isVisible = true
        }
    }

    override fun act(delta: Float) {
        updateContent()
        updateAnimation()
        super.act(delta)
    }
}