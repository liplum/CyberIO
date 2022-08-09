package net.liplum.common.ui

import arc.Core
import arc.math.Interp
import arc.scene.actions.Actions
import arc.scene.style.Drawable
import arc.scene.ui.layout.Table

class UIToast {
    var lastToastTable: Table? = null
    var lastToastLayout: Table? = null
    var lastToast: Long = 0
    var background: Drawable? = null
    fun postToastOnUI(content: Table) {
        val table = Table().apply {
            this@UIToast.background?.let { background(it) }
        }
        table.margin(12f)
        //add to table
        table.add(content)
        table.pack()
        //create container table which will align and move
        val container = Core.scene.table()
        container.top().add(table)
        container.setTranslation(0f, table.prefHeight)
        container.actions(
            Actions.translateBy(0f, -table.prefHeight, 1f, Interp.fade),
            Actions.delay(3f),  //nesting actions() calls is necessary so the right prefHeight() is used
            Actions.run {
                container.actions(
                    Actions.translateBy(0f, table.prefHeight, 1f, Interp.fade),
                    Actions.run {
                        lastToastTable = null
                        lastToastLayout = null
                    }, Actions.remove()
                )
            }
        )
        lastToastTable = container
        lastToastLayout = content
    }
}