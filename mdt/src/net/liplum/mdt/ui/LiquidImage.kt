package net.liplum.mdt.ui

import arc.scene.ui.Image
import arc.scene.ui.layout.Stack
import arc.scene.ui.layout.Table
import mindustry.core.UI
import mindustry.type.LiquidStack
import mindustry.ui.Styles
import plumy.core.assets.TR

open class LiquidImage() : Stack() {
    var fontScale = 0.85f

    constructor(tr: TR, amount: Float) : this() {
        add(Table { o: Table ->
            o.left()
            o.add(Image(tr)).size(32f)
        })

        add(Table { t: Table ->
            t.left().bottom()
            t.add(
                if (amount > 1000)
                    UI.formatAmount(amount.toLong())
                else
                    amount.toString()
            ).fontScale(fontScale)
            t.pack()
        })
    }

    constructor(tr: TR) : this() {
        val t = Table().left().bottom()

        add(Image(tr))
        add(t)
    }

    constructor(stack: LiquidStack) : this() {
        add(Table { o: Table ->
            o.left()
            o.add(Image(stack.liquid.uiIcon)).size(32f)
        })

        if (stack.amount > 0.1f) {
            add(Table { t: Table ->
                t.left().bottom()
                t.add(
                    if (stack.amount > 1000)
                        UI.formatAmount(stack.amount.toLong())
                    else
                        stack.amount.toInt().toString()
                ).style(Styles.outlineLabel).fontScale(fontScale)
                t.pack()
            })
        }
    }
}