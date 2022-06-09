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
import net.liplum.data.PayloadData

fun INetworkNode.buildNetworkDataList(table: Table) {
    table.add(ScrollPane(Table(Tex.wavepane).apply {
        network.forEachDataIndexed { i, node, data ->
            add(Table(Tex.button).apply {
                buildPayloadDataInfo(node, data)
            }).margin(5f).grow().size(Vars.iconXLarge * 2.5f)
            if ((i + 1) % 4 == 0) row()
        }
    }, Styles.defaultPane))
}

fun Table.buildPayloadDataInfo(node: INetworkNode, data: PayloadData) {
    add(Stack(
        Image(data.payload.icon()),
        Label("${data.id}"),
    )
    ).size(Vars.iconXLarge * 1.5f).row()
    val tile = node.tile
    add(Label { "${tile.x},${tile.y}" })
}

fun INetworkNode.buildNetworkDataListSelector(table: Table) {
    table.add(ScrollPane(Table(Tex.wavepane).apply {
        network.forEachDataIndexed { i, node, data ->
            add(Table(Tex.button).apply {
                buildPayloadDataInfoSelectorItem(this@buildNetworkDataListSelector, node, data)
            }).margin(5f).grow().size(Vars.iconXLarge * 2.5f)
            if ((i + 1) % 4 == 0) row()
        }
    }, Styles.defaultPane))
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
