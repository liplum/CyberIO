package net.liplum

import arc.Core.settings
import arc.math.Interp
import mindustry.Vars
import plumy.dsl.Setting
import plumy.core.ClientOnly
import net.liplum.update.Version2
import plumy.core.math.invoke

object Settings {
    @ClientOnly @JvmField var LinkOpacity = 1f
    @ClientOnly @JvmField var LinkArrowDensity = 15f
    @ClientOnly @JvmField var LinkArrowSpeed = 40f
    @ClientOnly @JvmField var AlwaysShowLink = true
    @ClientOnly @JvmField var LinkBloom = true
    @ClientOnly @JvmField var ShowLinkCircle = false
    @ClientOnly @JvmField var ShowWirelessTowerCircle = true
    // input [0,100] -> output [30,0]
    fun percentage2Density(percent: Int): Float =
        Interp.pow2Out(1f - percent / 100f) * 30f
    @JvmStatic
    fun updateSettings() {
        LinkOpacity = settings.getInt(R.Setting.LinkOpacity, 100) / 100f
        LinkArrowSpeed = settings.getInt(R.Setting.LinkAnimationSpeed, 40).toFloat()
        AlwaysShowLink = settings.getBool(R.Setting.AlwaysShowLink, true)
        LinkBloom = settings.getBool(R.Setting.LinkBloom, true)
        ShowLinkCircle = settings.getBool(R.Setting.ShowLinkCircle, false)
        ShowWirelessTowerCircle = settings.getBool(R.Setting.ShowWirelessTowerCircle, true)
    }

    internal val settingsMap = HashMap<String, Setting<Settings, *>>()
    var ShouldShowWelcome: Boolean by Setting(R.Setting.ShowWelcome, true)
    var ClickWelcomeTimes: Int by Setting(R.Setting.ClickWelcomeTimes, 0)
    @Deprecated(
        "Use Settings.LastWelcomeID instead",
        ReplaceWith("Settings.LastWelcomeID"),
        level = DeprecationLevel.ERROR
    )
    var LastWelcome: Int by Setting("", 0)
    /** Used to prevent from displaying the same welcome words as last time */
    var LastWelcomeID: String by Setting(R.Setting.LastWelcomeID, "")
    /** Represent the major version of Cyber IO */
    var CioVersion: String by Setting(R.Setting.Version, "v0")
    /** Whether the update should show up when judging the welcome content. */
    var ShowUpdate: Boolean by Setting(R.Setting.ShowUpdate, !Vars.steam)
    /** Represent the first time player installed Cyber IO */
    var FirstInstallationTime: Long by Setting(R.Setting.FirstInstallationTime, -1L)
    /** Represent how many times player loaded Cyber IO's content */
    var CyberIOLoadedTimes: Int by Setting(R.Setting.CyberIOLoadedTimes, 0)
    /** Represent how many times player loaded Cyber IO's main class */
    var ClassLoadedTimes: Int by Setting(R.Setting.ClassLoadedTimes, 0)
    /**
     * It will be updated when [CioMod.init] call ends.
     * So if you want to get the real last play time, please check [CioMod.lastPlayTime].
     */
    var LastPlayTime: Long by Setting(R.Setting.LastPlayTime, -1L)
    @Deprecated(
        "GitHub Mirror support is removed in v5.0",
        level = DeprecationLevel.ERROR
    )
    var GitHubMirrorUrl: String by Setting("", "")
    var ShaderRootPath: String by Setting(R.Setting.ShaderRootPath, "")
    var ContentSpecific: String by Setting(R.Setting.ContentSpecific, ContentSpec.Vanilla.id)
    /** Last skipped update. If it equals to current new version detected, it will skip the update dialog. */
    var LastSkippedUpdate: String by Setting(R.Setting.LastSkippedUpdate, Version2.Zero.toString())

    internal inline fun <reified T> Setting(key: String, default: T): Setting<Settings, T> =
        Setting(key, default) { settingsMap[it] = this }
}
