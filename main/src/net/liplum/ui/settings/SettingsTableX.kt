package net.liplum.ui.settings

import arc.Core
import arc.scene.ui.layout.Cell
import mindustry.ui.dialogs.SettingsMenuDialog
import net.liplum.common.delegate.Delegate
import plumy.dsl.bundle
import kotlin.jvm.functions.Function0
import kotlin.jvm.functions.Function1

class SettingsTableX : SettingsMenuDialog.SettingsTable() {
    val onReset = Delegate()
    var genHeader: (SettingsTableX) -> Unit = {}
    private var rebuilding = false
    fun onSettingsReset(handler: Function0<Unit>) {
        onReset.add(handler)
    }

    override fun rebuild() {
        if (rebuilding) return
        rebuilding = true

        try {
            clearChildren()
            genHeader(this)
            for (setting in list) {
                if (setting is ISettingCondition && !setting.canShow()) continue
                setting.add(this)
            }
            button(
                bundle("settings.reset", "Reset to Defaults")
            ) {
                resetSettings()
            }.margin(14f).width(240f).pad(6f)
            for (cell: Cell<*> in cells) {
                cell.pad(5f)
            }
        } finally {
            rebuilding = false
        }
    }

    private fun resetSettings() {
        for (setting in list) {
            if (setting.name != null && setting.title != null) {
                Core.settings.put(
                    setting.name,
                    Core.settings.getDefault(setting.name)
                )
            }
        }
        onReset.invoke()
        rebuild()
    }
}
