package net.liplum.lib.ui

import arc.input.KeyCode
import arc.math.geom.Vec2
import arc.scene.Element
import arc.scene.event.InputEvent
import arc.scene.event.InputListener
import net.liplum.lib.math.component1
import net.liplum.lib.math.component2

class Dragger(
    val target: Element
) : InputListener() {
    private var temp = Vec2()
    var lastX = 0f
    var lastY = 0f
    override fun touchDown(
        event: InputEvent, x: Float, y: Float, pointer: Int, button: KeyCode
    ): Boolean {
        val (svX, svY) = target.localToStageCoordinates(temp.set(x, y))
        lastX = svX
        lastY = svY
        target.toFront()
        return true
    }

    override fun touchDragged(
        event: InputEvent, dx: Float, dy: Float, pointer: Int
    ) {
        val (svX, svY) = target.localToStageCoordinates(temp.set(dx, dy))
        target.setPosition(
            target.x + (svX - lastX),
            target.y + (svY - lastY)
        )
        lastX = svX
        lastY = svY
    }
}