package net.liplum

import arc.Core

object Settings {
    @JvmField var LinkOpacity = 1f
    @JvmStatic
    fun updateSettings() {
        LinkOpacity = Core.settings.getInt(R.Setting.LinkOpacity) / 100f
    }
}