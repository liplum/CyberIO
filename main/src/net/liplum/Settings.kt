package net.liplum

import arc.Core.settings
import mindustry.Vars
import net.liplum.common.Setting
import net.liplum.mdt.ClientOnly
import net.liplum.scripts.KeyNotFoundException
import net.liplum.update.Version2

object Settings {
    @ClientOnly @JvmField var LinkOpacity = 1f
    @ClientOnly @JvmField var LinkArrowDensity = 15f
    @ClientOnly @JvmField var LinkArrowSpeed = 40f
    @ClientOnly @JvmField var AlwaysShowLink = true
    @ClientOnly @JvmField var ShowLinkCircle = Vars.mobile
    @ClientOnly @JvmField var ShowWirelessTowerCircle = true
    @JvmStatic
    fun updateSettings() {
        LinkOpacity = settings.getInt(R.Setting.LinkOpacity, 100) / 100f
        LinkArrowSpeed = settings.getInt(R.Setting.LinkAnimationSpeed, 40).toFloat()
        AlwaysShowLink = settings.getBool(R.Setting.AlwaysShowLink, true)
        ShowLinkCircle = settings.getBool(R.Setting.ShowLinkCircle, Vars.mobile)
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
    var LastWelcome: Int by Setting(R.Setting.LastWelcome, 0)
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
    var GitHubMirrorUrl: String by Setting(R.Setting.GitHubMirrorUrl, Meta.GitHubMirrorUrl)
    var ShaderRootPath: String by Setting(R.Setting.ShaderRootPath, "")
    var ContentSpecific: String by Setting(R.Setting.ContentSpecific, ContentSpec.Vanilla.id)
    /** Last skipped update. If it equals to current new version detected, it will skip the update dialog. */
    var LastSkippedUpdate: String by Setting(R.Setting.LastSkippedUpdate, Version2.Zero.toString())
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T =
        settingsMap[key]?.let { it[key] as T } ?: throw KeyNotFoundException("Can't find $key in Cyber IO settings.")

    internal inline fun <reified T> Setting(key: String, default: T): Setting<Settings, T> =
        Setting(key, default) { settingsMap[it] = this }
}
