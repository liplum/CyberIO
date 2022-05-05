package net.liplum.lib.ui.settings

import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting

class AnySetting(
    val ctor: AnySetting.(SettingsTable) -> Unit,
) : Setting(null), ISettingCondition {
    var canShow: () -> Boolean = { true }
    override fun canShow(): Boolean =
        this.canShow.invoke()

    override fun add(table: SettingsTable) {
        this.ctor(table)
        table.row()
    }

    companion object {
        fun SettingsTable.addAny(
            ctor: AnySetting.(SettingsTable) -> Unit,
        ): AnySetting {
            val res = AnySetting(ctor)
            settings.add(res)
            rebuild()
            return res
        }
    }
}