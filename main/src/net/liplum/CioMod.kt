package net.liplum

import arc.Core
import arc.Events
import arc.util.CommandHandler
import arc.util.Time
import mindustry.Vars
import mindustry.game.EventType.*
import mindustry.io.JsonIO
import mindustry.mod.Mod
import mindustry.mod.Mods
import mindustry.ui.dialogs.PlanetDialog
import net.liplum.ConfigEntry.Companion.Config
import net.liplum.ContentSpec.Companion.resolveContentSpec
import net.liplum.Var.ContentSpecific
import net.liplum.data.LiplumCloud
import net.liplum.data.SharedRoom
import net.liplum.events.CioInitEvent
import net.liplum.events.CioLoadContentEvent
import net.liplum.gen.Contents
import net.liplum.gen.EventRegistry
import net.liplum.inputs.UnitTap
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.HeadlessOnly
import net.liplum.mdt.IsSteam
import net.liplum.mdt.animations.ganim.GlobalAnimation
import net.liplum.registries.CioShaderLoader
import net.liplum.registries.CioTechTree
import net.liplum.registries.ServerCommands.registerCioCommands
import net.liplum.registries.SpecificLoader
import net.liplum.render.TestShader
import net.liplum.scripts.NpcSystem
import net.liplum.update.Updater
import net.liplum.welcome.FirstLoaded
import net.liplum.welcome.Welcome
import net.liplum.welcome.WelcomeList
import java.io.File

class CioMod : Mod() {
    companion object {
        @JvmField var ContentLoaded = false
        lateinit var Info: Mods.LoadedMod
        @JvmField val jarFile = CioMod::class.java.protectionDomain?.let {
            File(it.codeSource.location.toURI().path).let { f ->
                if (f.isFile) f else null
            }
        }
        @JvmField var objCreated = false
        @JvmField var lastPlayTime: Long = -1

        init {
            try {
                if (!Core.settings.has(Meta.RepoInSettingsKey))
                    Core.settings.put(Meta.RepoInSettingsKey, Meta.Repo)
                if (!Vars.headless && Vars.clientLoaded && !objCreated) {
                    FirstLoaded.tryRecord()
                    FirstLoaded.load()
                    Time.run(15f) {
                        FirstLoaded.showDialog()
                    }
                }
                val former = Core.settings.getInt("cyber-io-clz-loaded-times", 0)
                Core.settings.put("cyber-io-clz-loaded-times", former + 1)
            } catch (_: Exception) {
            }
        }
    }
    /**
     * # Calling time node
     * 1. constructor
     * 2. FileTreeInitEvent
     * 3. loadContent()
     * 4. init()
     * 5. ClientLoadEvent
     */
    init {
        objCreated = true
        lastPlayTime = Settings.LastPlayTime
        CLog.info("v${Meta.DetailedVersion} loading started.")
        (net.liplum.mdt.IsClient and !IsSteam) {
            Updater.fetchLatestVersion(updateInfoFileURL = Meta.UpdateInfoURL)
        }
        HeadlessOnly {
            ConfigEntry.load()
            ContentSpecific = Config.ContentSpecific.resolveContentSpec()
            Updater.fetchLatestVersion(updateInfoFileURL = Config.CheckUpdateInfoURL)
            Updater.checkHeadlessUpdate()
        }
        ClientOnly {
            ContentSpecific = Settings.ContentSpecific.resolveContentSpec()
        }
        SpecificLoader.handle()
        EventRegistry.registerAll()
        ClientOnly {
            GL.handleCompatible()
        }
        Events.on(ClientLoadEvent::class.java) {
            //show welcome dialog upon startup
            Time.runTask(15f) {
                Welcome.showWelcomeDialog()
            }
        }
        Events.on(FileTreeInitEvent::class.java) {
            ClientOnly {
                Core.app.post {
                    CioShaderLoader.init()
                    WelcomeList.loadList()
                    Welcome.load()
                    DebugOnly {
                        TestShader.load()
                    }
                    /* TODO: Add real story mode in v5?
                    DebugOnly {
                        Script.init()
                        Script.initInterpreter()
                        Script.loadStory("OnTheShip.Captain.Introduction")
                    }*/
                }
            }
        }
        Events.on(DisposeEvent::class.java) {
            ClientOnly {
                CioShaderLoader.dispose()
            }
        }
    }

    override fun init() {
        CLog.info("v${Meta.DetailedVersion} $ContentSpecific initializing...")
        Var.AnimUpdateFrequency = if (Vars.mobile || Vars.testMobile) 10f else 5f
        Events.fire(CioInitEvent())
        DebugOnly {
            // Cloud is developing
            PlanetDialog.debugSelect = true
            JsonIO.json.addClassTag(SharedRoom::class.java.name, SharedRoom::class.java)
            Events.on(WorldLoadEvent::class.java) {
                LiplumCloud.reset()
                LiplumCloud.read()
            }
            Events.on(SaveWriteEvent::class.java) {
                LiplumCloud.reset()
                LiplumCloud.save()
            }
            ClientOnly {
                NpcSystem.register()
                Core.input.addProcessor(UnitTap)
            }
        }

        Settings.updateSettings()
        //RecipeCenter.recordAllRecipes()
        ResourceLoader.loadAllResources()
        CLog.info("v${Meta.DetailedVersion} $ContentSpecific initialized.")
        Settings.LastPlayTime = System.currentTimeMillis()
        Settings.CyberIOLoadedTimes++
    }

    override fun loadContent() {
        Info = Vars.mods.locateMod(Meta.ModID)
        val meta = Info.meta
        meta.version = ContentSpecific.suffixModVersion(meta.version)
        ClientOnly {
            meta.subtitle = "[#${ContentSpecific.color}]${Meta.Version} ${ContentSpecific.i18nName}[]"
        }
        Events.fire(CioLoadContentEvent())
        Contents.load()
        CioTechTree.loadAll()
        GlobalAnimation.CanPlay = true
        ContentLoaded = true
        CLog.info("v${Meta.DetailedVersion} $ContentSpecific mod's contents loaded.")
    }
    @HeadlessOnly
    override fun registerServerCommands(handler: CommandHandler) {
        handler.registerCioCommands()
    }
}