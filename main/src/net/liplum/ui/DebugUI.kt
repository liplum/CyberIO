package net.liplum.ui

import arc.Core
import arc.Events
import arc.scene.Group
import arc.scene.event.Touchable
import arc.scene.ui.ImageButton
import arc.scene.ui.Label
import arc.scene.ui.ScrollPane
import arc.scene.ui.TextField
import arc.scene.ui.layout.Table
import arc.scene.ui.layout.WidgetGroup
import arc.struct.Seq
import arc.util.Interval
import mindustry.Vars
import mindustry.game.EventType
import mindustry.gen.*
import mindustry.graphics.Pal
import net.liplum.CLog
import net.liplum.Var
import net.liplum.annotations.Only
import net.liplum.annotations.SubscribeEvent
import plumy.core.arc.tinted
import net.liplum.event.CioInitEvent
import net.liplum.common.util.allFieldsIncludeParents
import net.liplum.common.util.directSuperClass
import net.liplum.utils.Screen
import net.liplum.render.Shape
import net.liplum.ui.attach.Dragger.Companion.dragToMove
import java.lang.reflect.Field

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

    val entityList = ArrayList<Entityc>(64)
    val starList = Seq<Entityc>(8)
    val entityClz2Field = HashMap<Class<out Entityc>, ArrayList<Field>>()
    val timer = Interval(10)
    var timerID = 0
    val updateEntityListTimer = timerID++
    var inspectedEntity: Entityc? = null
    fun addEntityInspector(debug: Group) {
        val listView = Table()
        val inspectorPanel = Table()
        lateinit var search: TextField
        fun rebuildInspectorPanel() {
            inspectorPanel.clearChildren()
            val entity = inspectedEntity ?: return
            val fields = entityClz2Field.getOrPut(entity.javaClass) {
                ArrayList<Field>().apply {
                    entity.javaClass.allFieldsIncludeParents(this) {
                        it.type == String::class.java ||
                                it.type == Int::class.java ||
                                it.type == Float::class.java
                    }
                }
            }
            inspectorPanel.add(Table().apply {
                button(Icon.refresh) {
                    rebuildInspectorPanel()
                }.pad(5f)
                button(Icon.cancel) {
                    when (entity) {
                        is Healthc -> entity.kill()
                        else -> entity.remove()
                    }
                    inspectedEntity = null
                }.pad(5f)
            }).row()
            for (field in fields) {
                inspectorPanel.add(Table().apply {
                    var newValue = field.get(entity)?.toString() ?: "null"
                    add(field.name).pad(5f).grow()
                    this.field(newValue) {
                        newValue = it
                    }.pad(5f).grow()
                    this.button(Icon.pencil) {
                        try {
                            val value = when (field.type) {
                                String::class.java -> newValue
                                Int::class.java -> newValue.toInt()
                                Float::class.java -> newValue.toFloat()
                                else -> return@button
                            }
                            field.set(entity, value)
                        } catch (e: Exception) {
                            CLog.err(e)
                        }
                    }
                }).grow().row()
            }
        }

        var lastEntityNumber = 0
        fun rebuildEntityList() {
            if (Groups.all.size() == lastEntityNumber) return
            lastEntityNumber = Groups.all.size()
            if (!Vars.state.isGame) return
            val searchText = search.text.lowercase().trim()
            if (searchText.isEmpty()) return
            listView.clearChildren()
            entityList.clear()
            Groups.all.each {
                if (it in starList) return@each
                if (searchText == "*")// wildcard
                    entityList.add(it)
                else {
                    if (it.directSuperClass.name.lowercase().contains(searchText) ||
                        it.toString().lowercase().contains(searchText)
                    ) {
                        entityList.add(it)
                    }
                }
            }
            starList.forEach {
                listView.add(Table(Tex.button).apply {
                    add(Table().apply {
                        add(ImageButton(Shape.starActive).apply {
                            clicked {
                                starList.remove(it)
                                rebuildEntityList()
                            }
                            getCell(image).size(Vars.iconSmall)
                        })
                    }).left()
                    add(Table().apply {
                        clicked {
                            inspectedEntity = if (it.isAdded) it else null
                            rebuildInspectorPanel()
                        }
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
                        add(ImageButton(Shape.starInactive).apply {
                            clicked {
                                starList.add(it)
                                rebuildEntityList()
                            }
                            getCell(image).size(Vars.iconSmall)
                        })
                    }).left()
                    add(Table().apply {
                        clicked {
                            inspectedEntity = if (it.isAdded) it else null
                            starList.add(it)
                            rebuildEntityList()
                            rebuildInspectorPanel()
                        }
                        add(it.javaClass.simpleName.tinted(Pal.accent)).row()
                        add("$it").row()
                    }).grow().right()
                }).fill()
                listView.row()
            }
        }

        debug.fill { t ->
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
                        rebuildEntityList()
                    }.apply {
                        growX()
                        minWidth(80f)
                    }.and {
                        messageText = "@players.search"
                    }
                    listView.update {
                        if (!isMouseOver && timer.get(updateEntityListTimer, 10f)) {
                            rebuildEntityList()
                        }
                    }
                    button(Icon.refresh) {
                        rebuildEntityList()
                    }.right()
                })
                row()
                add(Table().apply {
                    add(ScrollPane(listView)).autoLoseFocus().apply {
                        minWidth(120f)
                        maxHeight(600f)
                        fill()
                    }
                    add(ScrollPane(Table().apply {
                        update {
                            inspectedEntity = if (inspectedEntity?.isAdded == true)
                                inspectedEntity else null
                        }
                        add(inspectorPanel)
                        rebuildInspectorPanel()
                    })).apply {
                        maxHeight(600f)
                        grow()
                    }.then {
                        visible { inspectedEntity != null }
                        autoLoseFocus()
                    }
                })
            })
            t.visible { Vars.state.isGame }
        }
        Events.on(EventType.WorldLoadEvent::class.java) {
            entityList.clear()
            inspectedEntity = null
            starList.clear()
        }
    }
}