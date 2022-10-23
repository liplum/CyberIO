package net.liplum.ui

import arc.func.Cons
import arc.func.Prov
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.ButtonGroup
import arc.scene.ui.ImageButton
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.Scl
import arc.scene.ui.layout.Table
import arc.struct.Seq
import arc.util.Nullable
import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.gen.Tex
import mindustry.ui.Styles
import mindustry.world.Block
import mindustry.world.blocks.ItemSelection
import plumy.core.arc.invoke
import plumy.core.assets.TR

/**
 * Copy from [ItemSelection.buildTable]
 */
fun <T : UnlockableContent> Table.addItemSelector(
    @Nullable block: Block?,
    items: Seq<T>,
    holder: () -> T?,
    closeSelect: Boolean,
    icon: T.() -> TR = { uiIcon },
    consumer: (T?) -> Unit,
) {
    val group = ButtonGroup<ImageButton>()
    group.setMinCheckCount(0)
    val cont = Table()
    cont.defaults().size(40f)
    var i = 0
    for (item in items) {
        if (!item.unlockedNow()) continue
        val button = cont.button(
            Tex.whiteui, Styles.clearTogglei, 24f
        ) {
            if (closeSelect)
                Vars.control.input.config.hideConfig()
        }.group(group).get()
        button.changed {
            consumer(if (button.isChecked) item else null)
        }
        button.style.imageUp = TextureRegionDrawable(item.icon())
        button.update {
            button.isChecked = holder() === item
        }
        if (i++ % 4 == 3) {
            cont.row()
        }
    }
    //add extra blank spaces so it looks nice
    if (i % 4 != 0) {
        val remaining = 4 - i % 4
        for (j in 0 until remaining) {
            cont.image(Styles.black6)
        }
    }
    val pane = ScrollPane(cont, Styles.smallPane)
    pane.setScrollingDisabled(true, false)
    if (block != null) {
        pane.setScrollYForce(block.selectScroll)
        pane.update {
            block.selectScroll = pane.scrollY
        }
    }
    pane.setOverscroll(false, false)
    this.add(pane).maxHeight(Scl.scl(40f * 5))
}

fun <T : UnlockableContent> Table.addItemSelector(
    @Nullable block: Block?,
    items: Seq<T>,
    holder: () -> T?,
    icon: T.() -> TR = { uiIcon },
    consumer: (T?) -> Unit,
) {
    this.addItemSelector(block, items, holder, true, icon, consumer)
}

fun <T : UnlockableContent> Table.addItemSelectorDefault(
    @Nullable block: Block?,
    items: Seq<T>,
    holder: () -> T?,
    consumer: (T?) -> Unit,
) {
    ItemSelection.buildTable(block, this, items, holder, consumer)
}