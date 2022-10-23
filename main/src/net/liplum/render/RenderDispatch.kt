package net.liplum.render

import arc.util.Time
import mindustry.game.EventType
import net.liplum.annotations.Subscribe
import plumy.core.ClientOnly
import java.util.*

typealias DrawTask = (DrawTaskInfo) -> Boolean

const val EndDraw = false
const val ContinueDrawing = true
@ClientOnly
object RenderDispatch {
    val preDraw = RenderTasks()
    val draw = RenderTasks()
    val drawOver = RenderTasks()
    val postDraw = RenderTasks()
    val uiDrawBegin = RenderTasks()
    val uiDrawEnd = RenderTasks()
    @Subscribe(EventType.Trigger.preDraw)
    fun onPreDraw() {
        preDraw.draw()
    }
    @Subscribe(EventType.Trigger.draw)
    fun onDraw() {
        draw.draw()
    }
    @Subscribe(EventType.Trigger.drawOver)
    fun onDrawOver() {
        drawOver.draw()
    }
    @Subscribe(EventType.Trigger.postDraw)
    fun onPostDraw() {
        postDraw.draw()
    }
    @Subscribe(EventType.Trigger.uiDrawBegin)
    fun onUiDrawBegin() {
        uiDrawBegin.draw()
    }
    @Subscribe(EventType.Trigger.uiDrawEnd)
    fun onUiDrawEnd() {
        uiDrawEnd.draw()
    }
}
@ClientOnly
class RenderTasks {
    private val tasks = LinkedList<DrawTaskInfo>()
    fun draw() {
        val it = tasks.iterator()
        while (it.hasNext()) {
            val info = it.next()
            info.time += Time.delta
            val ended = info.task(info)
            if (ended) it.remove()
        }
    }

    operator fun plusAssign(task: DrawTask) {
        tasks.add(DrawTaskInfo(task))
    }
}

class DrawTaskInfo(
    var task: DrawTask,
    var time: Float = 0f,
)