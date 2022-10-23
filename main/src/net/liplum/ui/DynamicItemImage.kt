package net.liplum.ui

import arc.graphics.g2d.TextureRegion
import arc.scene.ui.Image
import arc.scene.ui.Label
import arc.scene.ui.layout.Stack
import arc.scene.ui.layout.Table
import mindustry.core.UI
import mindustry.type.ItemStack
import mindustry.type.PayloadStack
import mindustry.ui.Styles

class DynamicItemImage(region: TextureRegion, amount: () -> Int) : Stack() {
    init {
        add(Table().apply {
            left()
            add(Image(region)).size(32f)
        })
        add(Table().apply {
            left().bottom()
            add(Label { if (amount() >= 1000) UI.formatAmount(amount().toLong()) else amount().toString() + "" })
                .style(Styles.outlineLabel)
            pack()
            visible {
                amount() > 0
            }
        })
    }

    constructor(stack: ItemStack) : this(stack.item.uiIcon, { stack.amount })
    constructor(stack: PayloadStack) : this(stack.item.uiIcon, { stack.amount })
}