package net.liplum.lib.ui

import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table

inline fun Table.addTable(
    func: Table.() -> Unit,
): Cell<Table> =
    add(Table().apply(func))
