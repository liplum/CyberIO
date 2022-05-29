package net.liplum.registries

import arc.util.CommandHandler
import net.liplum.ConfigEntry
import net.liplum.ConfigEntry.Companion.Config
import net.liplum.Meta
import net.liplum.R
import net.liplum.mdt.HeadlessOnly
import net.liplum.mdt.advanced.MapCleaner
import net.liplum.update.Updater

@HeadlessOnly
object ServerCommands {
    @HeadlessOnly
    @JvmStatic
    fun CommandHandler.registerCioCmds() {
        register(
            R.CMD.ReloadConfig,
            "Reload config file of CyberIO."
        ) {
            ConfigEntry.load()
        }
        register(
            R.CMD.ResetConfig,
            "Regenerate config file of CyberIO."
        ) {
            ConfigEntry.resetConfigFile()
        }
        register(
            R.CMD.CheckUpdate,
            "Check update of CyberIO."
        ) {
            Updater.fetchLatestVersion(updateInfoFileURL = Config.CheckUpdateInfoURL)
            Updater.checkHeadlessUpdate(shouldUpdateOverride = true)
        }
        register(
            R.CMD.ClearCyberIOConetnt,
            "Clear all contents from Cyber IO in current map."
        ) {
            MapCleaner.cleanCurrentMap(Meta.ModID)
        }
    }
}