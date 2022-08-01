package net.liplum.ui

import arc.scene.Element
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table

inline fun <T : Element> Cell<T>.then(func: T.() -> Unit): Cell<T> {
    this.get().func()
    return this
}

inline fun <T : Element> Cell<T>.and(func: T.() -> Unit): T {
    val element = this.get()
    element.func()
    return element
}

inline fun Table.addTable(
    func: Table.() -> Unit,
): Cell<Table> =
    add(Table().apply(func))

inline fun Table.addScrolledTable(
    func: Table.() -> Unit,
): Cell<ScrollPane> = add(ScrollPane(Table().apply(func)).apply {
    setFadeScrollBars(true)
    setSmoothScrolling(true)
})
