package net.liplum.ui.settings

import arc.scene.Element
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting
import net.liplum.ui.addTrackTooltip

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

    override fun addDesc(elem: Element) {
        if (description == null) return
        elem.addTrackTooltip(description)
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