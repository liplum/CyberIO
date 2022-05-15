package net.liplum.registries

import arc.util.CommandHandler
import net.liplum.Config
import net.liplum.HeadlessOnly
import net.liplum.R
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
            Config.load()
        }
        register(
            R.CMD.ResetConfig,
            "Regenerate config file of CyberIO."
        ) {
            Config.resetConfigFile()
        }
        register(
            R.CMD.CheckUpdate,
            "Check update of CyberIO."
        ) {
            Updater.fetchLatestVersion(Config.CheckUpdateInfoURL)
            Updater.checkHeadlessUpdate(shouldUpdateOverride = true)
        }
    }
}