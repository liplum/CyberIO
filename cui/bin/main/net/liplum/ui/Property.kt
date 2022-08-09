package net.liplum.ui

import arc.scene.Element
import arc.scene.event.InputEvent
import arc.scene.event.InputListener
import kotlin.properties.ReadOnlyProperty

fun <T : Element> T.isMouseOver(): ReadOnlyProperty<Any?, Boolean> {
    var isMouseOver = false
    addListener(object : InputListener() {
        override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Element?) {
            isMouseOver = true
        }

        override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Element?) {
            isMouseOver = false
        }
    })
    return ReadOnlyProperty { _, _ -> isMouseOver }
}