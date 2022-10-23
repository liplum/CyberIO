package net.liplum.ui.settings

import arc.Core
import arc.scene.Element
import arc.scene.event.Touchable
import arc.scene.ui.Label
import arc.scene.ui.Slider
import arc.scene.ui.layout.Table
import mindustry.ui.Styles
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting
import mindustry.ui.dialogs.SettingsMenuDialog.StringProcessor
import net.liplum.ui.addTrackTooltip

operator fun StringProcessor.invoke(i: Int): String =
    this.get(i)

class SliderSettingX(
    name: String,
    val def: Int, val min: Int, val max: Int, val step: Int,
    val str: StringProcessor = StringProcessor { it.toString() },
    val onChanged: () -> Unit = {},
) : Setting(name), ISettingCondition {
    var canShow: () -> Boolean = { true }
    override fun canShow(): Boolean =
        this.canShow.invoke()

    override fun add(table: SettingsTable) {
        val slider = Slider(min.toFloat(), max.toFloat(), step.toFloat(), false)
        slider.value = Core.settings.getInt(name).toFloat()
        val value = Label("", Styles.outlineLabel)
        val content = Table()
        content.add(title, Styles.outlineLabel).left().growX().wrap()
        content.add(value).padLeft(10f).right()
        content.margin(3f, 33f, 3f, 33f)
        content.touchable = Touchable.disabled

        slider.changed {
            Core.settings.put(name, slider.value.toInt())
            value.setText(str(slider.value.toInt()))
            onChanged()
        }

        slider.change()

        addDesc(
            table
                .stack(slider, content)
                .width((Core.graphics.width / 1.2f).coerceAtMost(460f))
                .left()
                .padTop(4f).get()
        )
        table.row()
    }

    override fun addDesc(elem: Element) {
        if (description == null) return
        elem.addTrackTooltip(description)
    }

    companion object {
        fun SettingsTable.addSliderSettingX(
            name: String,
            def: Int, min: Int, max: Int, step: Int,
            str: StringProcessor = StringProcessor { it.toString() },
            onChanged: () -> Unit = {},
        ): SliderSettingX = SliderSettingX(name, def, min, max, step, str, onChanged).apply {
            settings.add(this)
            Core.settings.defaults(name, def)
            rebuild()
        }
    }
}