package net.liplum.ui.control

import arc.scene.ui.layout.Table
import net.liplum.ui.animation.AnimatedVisibility
import net.liplum.ui.animation.AnimationSpec
import java.util.*

open class DelayTable(
    val duration: Float = 30f,
    var animation: AnimationSpec,
) : Table() {
    var queue = LinkedList<Item>()
    var curItem: Item? = null
    fun delay(content: Table) {
        queue.add(
            Item(
                content,
                AnimatedVisibility(
                    duration = duration,
                    spec = animation
                )
            )
        )
    }

    fun resetAnimation() {
        curItem?.apply {
            visibility.restart()
        }
    }

    protected fun updateAnimation() {
        curItem?.apply {
            visibility.updateTimer()
            visibility.update(table)
        }
    }

    protected fun updateContent() {
        if (queue.isNotEmpty()) {
            // try adding
            val animating = curItem
            if (animating != null) {
                val visibility = animating.visibility
                visibility.isVisible = false
                if (!visibility.isEnd)
                    return // if hidden isn't ended
            }
            // if hidden was ended or nothing before
            clear()
            val item = queue.poll()
            add(item.table).grow()
            curItem = item
        }
    }

    override fun act(delta: Float) {
        updateContent()
        updateAnimation()
        super.act(delta)
    }

    data class Item(
        val table: Table,
        val visibility: AnimatedVisibility,
    )
}