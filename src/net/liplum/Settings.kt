package net.liplum

import arc.Core

object Settings {
    @ClientOnly @JvmField var LinkOpacity = 1f
    @ClientOnly @JvmField var AlwaysShowLink = false
    @JvmStatic
    fun updateSettings() {
        LinkOpacity = Core.settings.getInt(R.Setting.LinkOpacity) / 100f
        AlwaysShowLink = Core.settings.getBool(R.Setting.AlwaysShowLink)
    }
}