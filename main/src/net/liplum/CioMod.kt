package net.liplum

import arc.Core
import arc.Events
import arc.util.CommandHandler
import arc.util.Time
import mindustry.Vars
import mindustry.game.EventType.*
import mindustry.mod.Mod
import mindustry.mod.Mods
import mindustry.ui.dialogs.PlanetDialog
import net.liplum.ConfigEntry.Companion.Config
import net.liplum.ContentSpec.Companion.resolveContentSpec
import net.liplum.ContentSpec.Companion.tryResolveContentSpec
import net.liplum.Var.ContentSpecific
import net.liplum.event.CioInitEvent
import net.liplum.event.CioLoadContentEvent
import net.liplum.event.UnitTap
import net.liplum.gen.Contents
import net.liplum.gen.EventRegistry
import net.liplum.utils.IsClient
import net.liplum.utils.IsSteam
import net.liplum.utils.safeCall
import net.liplum.registry.CioShaderLoader
import net.liplum.registry.CioTechTree
import net.liplum.registry.ServerCommand.registerCioCommands
import net.liplum.registry.SpecificLoader
import net.liplum.render.GlobalAnimation
import net.liplum.render.TestShader
import net.liplum.update.Updater
import net.liplum.welcome.FirstLoaded
import net.liplum.welcome.Welcome
import plumy.core.ClientOnly
import plumy.core.HeadlessOnly
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
        // Vars.mobile = true
        lastPlayTime = Settings.LastPlayTime
        CLog.info("v${Meta.DetailedVersion} loading started.")
        (IsClient and !IsSteam) {
            Updater.fetchLatestVersion()
        }
        HeadlessOnly {
            ConfigEntry.load()
            ContentSpecific = Config.ContentSpecific.resolveContentSpec()
            Updater.fetchLatestVersion()
            Updater.Headless.tryUpdateHeadless()
        }
        safeCall {
            CLog.info(
                """
                [Java Info]
                java.vm.name: ${System.getProperty("java.vm.name")}
                java.vm.vendor: ${System.getProperty("java.vm.vendor")}
                java.vm.specification.version: ${System.getProperty("java.vm.specification.version")}
                java.vm.version: ${System.getProperty("java.vm.version")}
                java.version: ${System.getProperty("java.version")}
                java.class.version: ${System.getProperty("java.class.version")}
                """.trimIndent()
            )
        }
        ClientOnly {
            ContentSpecific = Settings.ContentSpecific.resolveContentSpec()
        }
        safeCall {
            val debugSpec = System.getenv("CYBERIO_SPEC")
            if (debugSpec != null) {
                val env = debugSpec.tryResolveContentSpec()
                if (env != null) {
                    ContentSpecific = env
                    Settings.ContentSpecific = env.id
                }
            }
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
                    safeCall {
                        Welcome.load()
                    }
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
            PlanetDialog.debugSelect = true
            ClientOnly {
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
            meta.subtitle = "[#${ContentSpecific.color}]${Meta.DetailedVersion} ${ContentSpecific.i18nName}[]"
        }
        VanillaSpec {
            Var.HoloWallTintAlpha = 0.6423f
            Var.HoloUnitTintAlpha = 0.404f
        }
        ErekirSpec {
            Var.HoloWallTintAlpha = 0.4688f
            Var.HoloUnitTintAlpha = 0.3442f
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