package net.liplum.ui

import arc.Core
import arc.scene.Group
import arc.scene.event.Touchable
import arc.scene.ui.ImageButton
import arc.scene.ui.Label
import arc.scene.ui.ScrollPane
import arc.scene.ui.TextField
import arc.scene.ui.layout.Table
import arc.scene.ui.layout.WidgetGroup
import arc.util.Interval
import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.gen.Entityc
import mindustry.gen.Groups
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.graphics.Pal
import net.liplum.Var
import net.liplum.annotations.Only
import net.liplum.annotations.SubscribeEvent
import net.liplum.events.CioInitEvent
import net.liplum.lib.ui.and
import net.liplum.lib.ui.autoLoseFocus
import net.liplum.lib.ui.dragToMove
import net.liplum.lib.ui.isMouseOver
import net.liplum.lib.utils.directSuperClass
import net.liplum.lib.utils.tinted
import net.liplum.mdt.Screen
import net.liplum.mdt.lock
import net.liplum.mdt.ui.`lock or unlock`
import net.liplum.render.Shapes

object DebugUI {
    @JvmStatic
    @SubscribeEvent(CioInitEvent::class, Only.client or Only.debug)
    fun appendUI() {
        addMousePosition()
        val debug = WidgetGroup().apply {
            setFillParent(true)
            touchable = Touchable.childrenOnly
        }
        Core.scene.add(debug)
        addUnlockContent(debug)
        addEntityInspector(debug)
    }

    fun addMousePosition() {
        val hudGroup = Vars.ui.hudGroup
        val minimap = hudGroup.find<Table>("minimap/position")
        minimap?.apply {
            row()
            label {
                val build = Screen.tileOnMouse()?.build
                if (build != null)
                    "Build:${build.tile.x},${build.tile.y}"
                else
                    "Build:X,X"
            }.touchable(Touchable.disabled).name("mouse-position-build")
            row()
            label {
                val pos = Screen.worldOnMouse()
                "Build:${pos.x.toInt()},${pos.y.toInt()}"
            }.touchable(Touchable.disabled).name("mouse-position-world").uniformX()
            row()
        }
    }

    fun addUnlockContent(debug: Group) {
        debug.addChildAt(0, Table().apply {
            visible {
                Var.EnableUnlockContent
            }
            `lock or unlock`("Lock", UnlockableContent::lock)
            `lock or unlock`("Unlock", UnlockableContent::unlock)
        })
    }

    val entityList = ArrayList<Entityc>(64)
    val starList = ArrayList<Entityc>(8)
    val timer = Interval(10)
    var timerID = 0
    val updateEntityListTimer = timerID++
    var inspectedEntity: Entityc? = null
    fun addEntityInspector(debug: Group) {
        val listView = Table()
        lateinit var search: TextField
        lateinit var listPanel: Table
        fun rebuild() {
            if (!Vars.state.isGame) return
            val searchText = search.text.lowercase()
            if (searchText.isEmpty()) return
            listView.clearChildren()
            entityList.clear()
            Groups.all.each {
                if (it in starList) return@each
                if (searchText == "*")// wildcard
                    entityList.add(it)
                else {
                    if (it.javaClass.directSuperClass.name.lowercase().contains(searchText) ||
                        it.toString().lowercase().contains(searchText)
                    ) {
                        entityList.add(it)
                    }
                }
            }
            starList.forEach {
                listView.add(Table(Tex.button).apply {
                    add(Table().apply {
                        add(ImageButton(Shapes.starActive).apply {
                            clicked {
                                starList.remove(it)
                                rebuild()
                            }
                            getCell(image).size(Vars.iconSmall)
                        })
                    }).left()
                    add(Table().apply {
                        val label = Label(it.javaClass.simpleName.tinted(if (it.isAdded) Pal.accent else Pal.gray))
                        add(label).row()
                        add("$it").row()
                    }).grow().right()
                }).fill()
                listView.row()
            }
            entityList.sortBy { it.javaClass.simpleName }
            entityList.forEach {
                listView.add(Table(Tex.button).apply {
                    add(Table().apply {
                        add(ImageButton(Shapes.starInactive).apply {
                            clicked {
                                starList.add(it)
                                rebuild()
                            }
                            getCell(image).size(Vars.iconSmall)
                        })
                    }).left()
                    add(Table().apply {
                        add(it.javaClass.simpleName.tinted(Pal.accent)).row()
                        add("$it").row()
                    }).grow().right()
                }).fill()
                listView.row()
            }
        }

        debug.fill { t ->
            listPanel = t
            t.left()
            t.dragToMove()
            val isMouseOver: Boolean by t.isMouseOver()
            t.add(Table(Tex.wavepane).apply {
                visible {
                    Var.EnableEntityInspector
                }
                add(Table().apply {
                    image(Icon.zoom).padRight(8f)
                    search = field(null) {
                        rebuild()
                    }.apply {
                        growX()
                        minWidth(80f)
                    }.and {
                        messageText = "@players.search"
                    }
                    listView.update {
                        if (!isMouseOver && timer.get(updateEntityListTimer, 10f)) {
                            rebuild()
                        }
                    }
                    button(Icon.refresh) {
                        rebuild()
                    }.right()
                })
                row()
                add(Table().apply {
                    add(ScrollPane(listView)).autoLoseFocus().apply {
                        minWidth(120f)
                        maxHeight(600f)
                        fill()
                    }
                })
            })
            t.visible { Vars.state.isGame }
        }
    }
}