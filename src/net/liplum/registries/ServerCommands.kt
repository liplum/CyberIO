package net.liplum.registries

import arc.util.CommandHandler
import net.liplum.Config
import net.liplum.HeadlessOnly

object ServerCommands {
    @HeadlessOnly
    fun register(handler: CommandHandler) {
        handler.register("cio-reload-config", "Reload config file of CyberIO.") {
            Config.load()
        }
        handler.register("cio-reset-config", "Regenerate config file of CyberIO.") {
            Config.resetConfigFile()
        }
    }
}