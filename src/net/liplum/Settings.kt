package net.liplum

import arc.Core.settings

object Settings {
    @ClientOnly @JvmField var LinkOpacity = 1f
    @ClientOnly @JvmField var LinkArrowDensity = 15f
    @ClientOnly @JvmField var AlwaysShowLink = false
    @JvmStatic
    fun updateSettings() {
        LinkOpacity = settings.getInt(R.Setting.LinkOpacity) / 100f
        AlwaysShowLink = settings.getBool(R.Setting.AlwaysShowLink)
    }

    var ShouldShowWelcome: Boolean
        get() = settings.getBool(R.Setting.ShowWelcome, true)
        set(value) = settings.put(R.Setting.ShowWelcome, value)
    var ClickWelcomeTimes: Int
        get() = settings.getInt(R.Setting.ClickWelcomeTimes, 0)
        set(value) = settings.put(R.Setting.ClickWelcomeTimes, value)
    var LastWelcome: Int
        get() = settings.getInt(R.Setting.LastWelcome, 0)
        set(value) = settings.put(R.Setting.LastWelcome, value)
    var CioVersion: String
        get() = settings.getString(R.Setting.Version, Meta.Version)
        set(value) = settings.put(R.Setting.Version, value)
    var ShowUpdate: Boolean
        get() = settings.getBool(R.Setting.ShowUpdate, true)
        set(value) = settings.put(R.Setting.ShowUpdate, value)
}