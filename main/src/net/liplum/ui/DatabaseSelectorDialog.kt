package net.liplum.ui

import arc.Core
import arc.graphics.Color
import arc.math.Mathf
import arc.scene.event.ClickListener
import arc.scene.event.HandCursorListener
import arc.scene.ui.Image
import arc.scene.ui.TextField
import arc.scene.ui.Tooltip
import arc.scene.ui.layout.Scl
import arc.scene.ui.layout.Table
import arc.util.Scaling
import arc.util.Time
import mindustry.Vars
import mindustry.ctype.ContentType
import mindustry.ctype.UnlockableContent
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.graphics.Pal
import mindustry.ui.dialogs.BaseDialog

open class DatabaseSelectorDialog : BaseDialog("") {
    companion object : DatabaseSelectorDialog()

    lateinit var search: TextField
    val all = Table()
    var onClick: (UnlockableContent) -> Unit = {}
    var filter: (UnlockableContent) -> Boolean = { true }

    init {
        shouldPause = true
        addCloseButton()
        shown { this.rebuild() }
        onResize { this.rebuild() }
        all.margin(20f).marginTop(0f)
        cont.table { s: Table ->
            s.image(Icon.zoom).padRight(8f)
            search = s.field(null) { rebuild() }.growX().get()
            search.messageText = "@players.search"
        }.fillX().padBottom(4f).row()
        cont.pane(all)
    }

    fun rebuild() {
        all.clear()
        val text = search.text
        val allContent = Vars.content.contentMap
        for ((j, content) in allContent.withIndex()) {
            val array = content.select {
                it is UnlockableContent && !it.isHidden && (text.isEmpty() || it.localizedName.lowercase()
                    .contains(text.lowercase())) && filter(it)
            }
            if (array.size == 0) continue
            val contentType = ContentType.all[j]

            all.add("@content." + contentType.name + ".name").growX().left().color(Pal.accent)
            all.row()
            all.image().growX().pad(5f).padLeft(0f).padRight(0f).height(3f).color(Pal.accent)
            all.row()
            all.addTable {
                left()
                val cols =
                    Mathf.clamp((Core.graphics.width - Scl.scl(30f)) / Scl.scl((32 + 10).toFloat()), 1f, 22f).toInt()
                for ((count, i) in (0 until array.size).withIndex()) {
                    val unlock = array[i] as UnlockableContent
                    val image = Image(unlock.uiIcon).setScaling(Scaling.fit)
                    add(image).size((8 * 4).toFloat()).pad(3f)
                    val listener = ClickListener()
                    image.addListener(listener)
                    if (!Vars.mobile) {
                        image.addListener(HandCursorListener())
                        image.update {
                            image.color.lerp(
                                if (!listener.isOver) Color.lightGray else Color.white,
                                Mathf.clamp(0.4f * Time.delta)
                            )
                        }
                    }
                    image.clicked {
                        onClick(unlock)
                    }
                    image.addListener(Tooltip { t: Table ->
                        t.background(Tex.button).add(
                            unlock.localizedName +
                                if (Core.settings.getBool("console")) "\n[gray]${unlock.name}" else ""
                        )
                    })
                    if ((count + 1) % cols == 0) {
                        row()
                    }
                }
            }.growX().left().padBottom(10f)
            all.row()
        }
    }
}