package net.liplum.registry

import arc.Core
import arc.util.CommandHandler
import arc.util.Log
import net.liplum.*
import net.liplum.ConfigEntry.Companion.Config
import net.liplum.ContentSpec.Companion.tryResolveContentSpec
import net.liplum.mdt.HeadlessOnly
import net.liplum.mdt.advanced.MapCleaner
import net.liplum.update.Updater

@HeadlessOnly
object ServerCommands {
    @HeadlessOnly
    @JvmStatic
    fun CommandHandler.registerCioCommands() {
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
        register(
            R.CMD.SwitchSpec,
            "<spec>",
            """
            Switch specific of Cyber IO:
            usage: cio-spec <spec>
            spec: ${ContentSpec.candidateList}
            """.trimIndent()
        ) {
            if (it.isNotEmpty()) {
                val to = it[0]
                if (to.equals(Var.ContentSpecific.id, ignoreCase = true)) {
                    Log.warn("CyberIO has been $to.")
                } else {
                    when (val spec = to.tryResolveContentSpec()) {
                        null -> Log.warn("Didn't find $to, please check if it's in ${ContentSpec.candidateList}.")
                        Var.ContentSpecific -> Log.warn("CyberIO has been $to.")
                        else -> {
                            Config.ContentSpecific = spec.id
                            Config.trySave()
                            Log.info("CyberIO switched to $spec, the game will restart soon.")
                            Core.app.exit()
                        }
                    }
                }
            }
        }
    }
}