package net.liplum.npc

import arc.scene.ui.Dialog
import arc.scene.ui.Label
import arc.util.Align
import mindustry.Vars
import mindustry.core.GameState
import mindustry.gen.Icon
import mindustry.gen.Sounds

open class NpcDialogLegacy : Dialog {
    constructor(title: String, style: DialogStyle) : super(title, style)
    constructor(title: String) : super(title)

    var wasPaused = false
    var shouldPause = false
    var titleGet = { "" }
    var titleLabel: Label

    init {
        setFillParent(true)
        titleLabel = Label(titleGet)
        titleLabel.style = Label.LabelStyle(style.titleFont, style.titleFontColor)
        titleLabel.setAlignment(Align.center)

        titleTable.clear()
        titleTable.add(titleLabel)
            .expandX()
            .fillX()
            .minWidth(0f)
            .row()
        hidden {
            if (shouldPause && Vars.state.isGame) {
                if (!wasPaused || Vars.net.active()) {
                    Vars.state.set(GameState.State.playing)
                }
            }
            Sounds.back.play()
        }

        shown {
            if (shouldPause && Vars.state.isGame) {
                wasPaused = Vars.state.`is`(GameState.State.paused)
                Vars.state.set(GameState.State.paused)
            }
        }
    }

    open fun addCloseListener() {
        closeOnBack()
    }

    override fun addCloseButton() {
        buttons.defaults().size(210f, 64f)
        buttons.button("@back", Icon.left) {
            this.hide()
        }.size(210f, 64f)
        addCloseListener()
    }

    override fun drawBackground(x: Float, y: Float) {
    }
}