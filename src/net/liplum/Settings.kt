package net.liplum

import arc.Core.settings

object Settings {
    @ClientOnly @JvmField var LinkOpacity = 1f
    @ClientOnly @JvmField var LinkArrowDensity = 15f
    @ClientOnly @JvmField var AlwaysShowLink = false
    @ClientOnly @JvmField var ShowLinkCircle = false
    @ClientOnly @JvmField var LinkSize = 4f
    @JvmStatic
    fun updateSettings() {
        LinkOpacity = settings.getInt(R.Setting.LinkOpacity, 100) / 100f
        AlwaysShowLink = settings.getBool(R.Setting.AlwaysShowLink)
        LinkSize = settings.getInt(R.Setting.LinkSize, 100) / 100f * 4f
        ShowLinkCircle = settings.getBool(R.Setting.ShowLinkCircle)
    }

    var ShouldShowWelcome: Boolean
        get() = settings.getBool(R.Setting.ShowWelcome, true)
        set(value) = settings.put(R.Setting.ShowWelcome, value)
    var ClickWelcomeTimes: Int
        get() = settings.getInt(R.Setting.ClickWelcomeTimes, 0)
        set(value) = settings.put(R.Setting.ClickWelcomeTimes, value)
    @Deprecated("Use Settings.LastWelcomeID instead", level = DeprecationLevel.ERROR)
    var LastWelcome: Int
        get() = settings.getInt(R.Setting.LastWelcome, 0)
        set(value) = settings.put(R.Setting.LastWelcome, value)
    var LastWelcomeID: String
        get() = settings.getString(R.Setting.LastWelcomeID, "")
        set(value) = settings.put(R.Setting.LastWelcomeID, value)
    var CioVersion: String
        get() = settings.getString(R.Setting.Version, "v0")
        set(value) = settings.put(R.Setting.Version, value)
    var ShowUpdate: Boolean
        get() = settings.getBool(R.Setting.ShowUpdate, true)
        set(value) = settings.put(R.Setting.ShowUpdate, value)
    var FirstInstallationTime: Long
        get() = settings.getLong(R.Setting.FirstInstallationTime, -1)
        set(value) = settings.put(R.Setting.FirstInstallationTime, value)
    /**
     * It will be updated when [CioMod.init] call ends.
     * So if you want to get the real last play time, please check [CioMod.lastPlayTime].
     */
    var LastPlayTime: Long
        get() = settings.getLong(R.Setting.LastPlayTime, -1)
        set(value) = settings.put(R.Setting.LastPlayTime, value)
}