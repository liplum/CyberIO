package net.liplum.lib.ui.settings

import arc.Core
import arc.scene.event.Touchable
import arc.scene.ui.Label
import arc.scene.ui.Slider
import arc.scene.ui.layout.Table
import mindustry.ui.Styles
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.SliderSetting
import mindustry.ui.dialogs.SettingsMenuDialog.StringProcessor

operator fun StringProcessor.invoke(i: Int): String =
    this.get(i)

class SliderSettingX(
    name: String,
    def: Int, val min: Int, val max: Int, val step: Int,
    val onChanged: () -> Unit = {},
    val str: StringProcessor = StringProcessor { it.toString() }
) : SliderSetting(name, def, min, max, step, str) {
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
}