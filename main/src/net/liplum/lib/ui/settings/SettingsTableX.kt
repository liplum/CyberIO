package net.liplum.lib.ui.settings

import arc.Core
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import net.liplum.lib.bundle
import net.liplum.lib.delegates.Delegate

class SettingsTableX : SettingsTable() {
    val onReset = Delegate()
    fun onSettingsReset(handler: () -> Unit) {
        onReset.add(handler)
    }

    var genHeader: (SettingsTableX) -> Unit = {}
        set(value) {
            field = value
            rebuild()
        }

    override fun rebuild() {
        clearChildren()

        genHeader(this)
        for (setting in list) {
            if (setting is ISettingCondition && !setting.canShow()) continue
            setting.add(this)
        }
        button("settings.reset".bundle("Reset to Defaults")) {
            for (setting in list) {
                if (setting.name == null || setting.title == null) continue
                Core.settings.put(setting.name, Core.settings.getDefault(setting.name))
            }
            onReset()
            rebuild()
        }.margin(14f).width(240f).pad(6f)
    }
}