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
import net.liplum.ContentSpec.Companion.resolveContentSpec
import net.liplum.blocks.cloud.LiplumCloud
import net.liplum.blocks.cloud.SharedRoom
import net.liplum.events.CioInitEvent
import net.liplum.events.CioLoadContentEvent
import net.liplum.gen.Contents
import net.liplum.gen.EventRegistry
import net.liplum.inputs.UnitTap
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.HeadlessOnly
import net.liplum.mdt.NotSteam
import net.liplum.mdt.OnlyClient
import net.liplum.mdt.animations.ganim.GlobalAnimation
import net.liplum.registries.CioShaderLoader
import net.liplum.registries.CioTechTree
import net.liplum.registries.ServerCommands.registerCioCmds
import net.liplum.render.TestShader
import net.liplum.scripts.NpcSystem
import net.liplum.update.Updater
import net.liplum.welcome.FirstLoaded
import net.liplum.welcome.Welcome
import net.liplum.welcome.WelcomeList
import java.io.File

class CioMod : Mod() {
    companion object {
        @JvmField val IsClient = !Vars.headless
        @JvmField var DebugMode = true
        @JvmField var TestSteam = false
        @JvmField var TestGlCompatibility = false
        @JvmField var ExperimentalMode = false
        @JvmField var UpdateFrequency = 5f
        @JvmField var ContentSpecific = ContentSpec.Vanilla
        lateinit var Info: Mods.LoadedMod
        @JvmField val jarFile = CioMod::class.java.protectionDomain?.let {
            File(it.codeSource.location.toURI().path).let { f ->
                if (f.isFile) f else null
            }
        }
        @JvmField var objCreated = false
        @JvmField var lastPlayTime: Long = -1

        init {
            if (!Core.settings.has(Meta.RepoInSettingsKey))
                Core.settings.put(Meta.RepoInSettingsKey, Meta.Repo)
            if (IsClient && Vars.clientLoaded && !objCreated) {
                FirstLoaded.tryRecord()
                FirstLoaded.load()
                Time.run(15f) {
                    FirstLoaded.showDialog()
                }
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
        Clog.info("v${Meta.DetailedVersion} loading started.")
        (OnlyClient and NotSteam) {
            Updater.fetchLatestVersion(Meta.UpdateInfoURL)
        }
        HeadlessOnly {
            Config.load()
            ContentSpecific = Config.ContentSpecific.resolveContentSpec()
            Updater.fetchLatestVersion(Config.CheckUpdateInfoURL)
            Updater.checkHeadlessUpdate()
        }
        ClientOnly {
            ContentSpecific = Settings.ContentSpecific.resolveContentSpec()
        }
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
                    /* TODO: Add real story mode in v4?
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
        Clog.info("v${Meta.DetailedVersion} $ContentSpecific initializing...")
        UpdateFrequency = if (Vars.mobile || Vars.testMobile) 10f else 5f
        Events.fire(CioInitEvent())
        DebugOnly {
            // Cloud is developing
            JsonIO.json.addClassTag(SharedRoom::class.java.name, SharedRoom::class.java)
            Events.on(WorldLoadEvent::class.java) {
                LiplumCloud.reset()
                LiplumCloud.read()
            }
            Events.on(SaveWriteEvent::class.java) {
                LiplumCloud.reset()
                LiplumCloud.save()
            }
            Vars.enableConsole = true
            /*Events.on(WorldLoadEvent::class.java) {
                CioBlocks.sender.requirements = arrayOf()
                CioBlocks.receiver.requirements = arrayOf()
                CioBlocks.sender.requirements = arrayOf()
                Vars.world
                //Vars.state.rules.borderDarkness
            }*/
            ClientOnly {
                NpcSystem.register()
                Core.input.addProcessor(UnitTap)
            }
        }

        Settings.updateSettings()
        //RecipeCenter.recordAllRecipes()
        ResourceLoader.loadAllResources()
        Clog.info("v${Meta.DetailedVersion} $ContentSpecific initialized.")
        Settings.LastPlayTime = System.currentTimeMillis()
    }

    override fun loadContent() {
        Info = Vars.mods.locateMod(Meta.ModID)
        Info.meta.version = ContentSpecific.suffixModVersion(Info.meta.version)
        Events.fire(CioLoadContentEvent())
        Contents.load()
        CioTechTree.loadAll()
        GlobalAnimation.CanPlay = true
        Clog.info("v${Meta.DetailedVersion} $ContentSpecific mod's contents loaded.")
    }
    @HeadlessOnly
    override fun registerServerCommands(handler: CommandHandler) {
        handler.registerCioCmds()
    }
}