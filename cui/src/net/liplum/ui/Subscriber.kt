package net.liplum.ui

import arc.scene.Element
import arc.scene.event.VisibilityListener

inline fun <T : Element> T.onHidden(crossinline func: T.() -> Unit) {
    addListener(object : VisibilityListener() {
        override fun hidden(): Boolean {
            func()
            return false
        }
    })
}