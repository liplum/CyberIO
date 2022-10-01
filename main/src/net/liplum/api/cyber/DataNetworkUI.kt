package net.liplum.api.cyber

import arc.scene.event.Touchable
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.Image
import arc.scene.ui.Label
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.Stack
import arc.scene.ui.layout.Table
import arc.scene.utils.Elem
import mindustry.Vars
import mindustry.gen.Tex
import mindustry.ui.Styles
import net.liplum.Var.DataListItemMargin
import net.liplum.Var.DataListItemSize
import net.liplum.Var.DataListMaxItemInRow
import net.liplum.data.PayloadData
import net.liplum.ui.autoLoseFocus
import net.liplum.ui.onHidden
import net.liplum.ui.then

fun INetworkNode.buildNetworkDataList(table: Table) {
    val list = Table(Tex.wavepane)
    table.add(
        Table().apply {
            add(ScrollPane(list, Styles.defaultPane)).apply {
                minWidth(Vars.iconXLarge * 2.5f * 4f)
                minHeight(Vars.iconXLarge * 2.5f)
                grow()
            }
        }
    )
    fun rebuild() {
        list.clearChildren()
        network.forEachDataIndexed { i, node, data ->
            list.add(Table(Tex.button).apply {
                buildPayloadDataInfo(node, data)
            }).margin(5f).grow().size(DataListItemSize)
            if ((i + 1) % 4 == 0)
                list.row()
        }
    }
    rebuild()
    network.onDataInventoryChangedEvent += {
        rebuild()
    }
    table.onHidden {
        network.onDataInventoryChangedEvent.clear()
    }
}

fun Table.buildPayloadDataInfo(node: INetworkNode, data: PayloadData) {
    add(
        Stack(
            Image(data.payload.icon()),
            Label("${data.id}"),
        )
    ).size(Vars.iconXLarge * 1.5f).row()
    val tile = node.tile
    add(Label { "${tile.x},${tile.y}" })
}

fun INetworkNode.buildNetworkDataListSelector(table: Table) {
    val list = Table(Tex.wavepane).apply {
        left()
    }
    val sizePreItem = DataListItemSize + DataListItemMargin
    table.add(
        ScrollPane(list)
    ).apply {
        minWidth(sizePreItem * DataListMaxItemInRow)
        minHeight(sizePreItem)
        maxHeight(sizePreItem * DataListMaxItemInRow)
        fill()
    }.then {
        autoLoseFocus()
        setFadeScrollBars(true)
    }
    fun rebuild() {
        list.clearChildren()
        var count = 0
        network.forEachDataIndexed { i, node, data ->
            count++
            list.add(Table(Tex.button).apply {
                buildPayloadDataInfoSelectorItem(this@buildNetworkDataListSelector, node, data)
            }).margin(DataListItemMargin).size(DataListItemSize)
            if ((i + 1) % DataListMaxItemInRow == 0)
                list.row()
        }
        for (i in 0 until 4 - count)
            list.add(Table(Tex.button)).margin(DataListItemMargin).size(DataListItemSize)
    }
    rebuild()
    network.onDataInventoryChangedEvent += {
        rebuild()
    }
    table.onHidden {
        network.onDataInventoryChangedEvent.clear()
    }
}

fun Table.buildPayloadDataInfoSelectorItem(cur: INetworkNode, node: INetworkNode, data: PayloadData) {
    add(
        Stack(
            Elem.newImageButton(TextureRegionDrawable(data.payload.icon())) {
                cur.request = data.id
            },
            Label("${data.id}").apply {
                touchable = Touchable.disabled
            },
        )
    ).size(Vars.iconXLarge * 1.5f).row()
    val tile = node.tile
    add(Label { "${tile.x},${tile.y}" })
}
