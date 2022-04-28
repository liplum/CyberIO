package net.liplum.welcome

import arc.Events
import arc.math.Mathf
import arc.struct.ObjectMap
import arc.util.Time
import arc.util.serialization.JsonValue
import mindustry.Vars
import mindustry.game.EventType.Trigger
import mindustry.io.JsonIO
import net.liplum.*
import net.liplum.Settings.CioVersion
import net.liplum.Settings.ClickWelcomeTimes
import net.liplum.Settings.LastWelcome
import net.liplum.Settings.ShouldShowWelcome
import net.liplum.Settings.ShowUpdate
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.lib.Res
import net.liplum.update.Updater
import net.liplum.utils.ReferBundleWrapper
import net.liplum.utils.TR
import net.liplum.utils.atlas

@ClientOnly
object Welcome {
    var bundle: ReferBundleWrapper = ReferBundleWrapper.create()
    private var info = Info()
    private var entity = Entity(bundle, info)
    @JvmStatic
    fun showWelcomeDialog() {
        checkLastVersion()
        if (!Vars.steam && ShowUpdate && Updater.requireUpdate) {
            entity.tip = WelcomeList[info.update]
        } else if (ShouldShowWelcome) {
            // If it's the first time to play this version, let's show up the Zero Welcome.
            // Otherwise, roll until the result isn't as last one.
            if (ClickWelcomeTimes > 0) {
                entity.randomize(LastWelcome)
                LastWelcome = entity.number
            } else {
                entity.tip = WelcomeList[info.default]
            }
        }
        val template = entity.tip.template
        val dialog = template.gen(entity)
        dialog.show()
    }
    @JvmStatic
    fun modifierModInfo() {
        val meta = CioMod.Info.meta
        meta.displayName = "[#${R.C.Holo}]${meta.displayName}[]"
        Events.run(Trigger.update) {
            if (Time.time % 30 < 1f) {
                val color = RandomName.oneColor()
                meta.author = "$color${Meta.Author}[]"
            }
        }
    }
    @JvmStatic
    fun checkLastVersion() {
        val lastVersion = CioVersion
        if (lastVersion != Meta.Version) {
            ShouldShowWelcome = true
            ClickWelcomeTimes = 0
            ShowUpdate = true
            CioVersion = Meta.Version
        }
    }
    @JvmStatic
    fun recordClick() {
        ClickWelcomeTimes += 1
    }
    @JvmStatic
    @ClientOnly
    fun load() {
        loadBundle()
        loadInfo()
        //To load all templates
        Templates
    }
    @JvmStatic
    fun loadBundle() {
        bundle.loadMoreFrom("welcomes")
    }

    lateinit var infoJson: ObjectMap<String, JsonValue>
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun loadInfo() {
        val json = Res("WelcomeInfo.json").readAllText()
        infoJson = JsonIO.json.fromJson(ObjectMap::class.java, json) as ObjectMap<String, JsonValue>
        val curInfo = infoJson.get(Meta.Version)
        val default = curInfo.get("Default")?.asString() ?: "Default"
        val update = curInfo.get("Update")?.asString() ?: "Default"
        val scenes = curInfo.get("Scene").asStringArray()
        val parent: String? = curInfo.get("Parent")?.asString()
        info.default = default
        info.update = update
        val allScenes = HashSet<String>()
        allScenes.addAll(scenes)
        fun loadParent(parent: String) {
            val parentInfo = infoJson.get(parent)
            val parentScenes = parentInfo.get("Scene").asStringArray()
            val parentParent: String? = parentInfo.get("Parent")?.asString()
            allScenes.addAll(parentScenes)
            if (parentParent != null)
                loadParent(parentParent)
        }
        if (parent != null) {
            loadParent(parent)
        }
        info.scenes.addAll(allScenes)
    }

    class Info {
        var default = "Default"
        var update = "Default"
        var scenes: MutableList<String> = ArrayList()
        val sceneSize: Int
            get() = scenes.size

        operator fun get(index: Int) =
            if (index < 0)
                default
            else
                scenes[index]
    }

    class Entity(
        val bundle: ReferBundleWrapper,
        val info: Info,
    ) {
        var tip: WelcomeTip = WelcomeTip.Default
        var number: Int = 0
            private set

        fun randomize(avoid: Int) {
            val variants = info.sceneSize
            if (variants <= 0) {
                number = -1
            } else if (variants == 1) {
                number = 0
            } else {
                do {
                    number = Mathf.random(0, variants - 1)
                } while (number == avoid)
            }
            tip = WelcomeList[info[number]]
        }

        operator fun get(key: String) =
            bundle["$tip.$key"]

        val content: String
            get() = bundle["$tip"]
        val icon: TR
            get() = tip.iconPath.Cio.atlas()
    }
}
