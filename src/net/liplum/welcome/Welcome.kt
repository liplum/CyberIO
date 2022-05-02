package net.liplum.welcome

import arc.Core
import arc.Events
import arc.struct.ObjectMap
import arc.util.Time
import arc.util.serialization.JsonValue
import mindustry.game.EventType.Trigger
import mindustry.io.JsonIO
import net.liplum.*
import net.liplum.Settings.CioVersion
import net.liplum.Settings.ClickWelcomeTimes
import net.liplum.Settings.ShouldShowWelcome
import net.liplum.Settings.ShowUpdate
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.lib.Res
import net.liplum.utils.ReferBundleWrapper
import net.liplum.utils.TR
import net.liplum.utils.atlas

@ClientOnly
object Welcome {
    var bundle = ReferBundleWrapper.create()
    private var info = Info()
    private var entity = Entity(bundle, info)
    private var showWelcome = false
    @JvmStatic
    fun showWelcomeDialog() {
        checkLastVersion()
        judgeWelcome()
        if (showWelcome) {
            val template = entity.tip.template
            val dialog = template.gen(entity)
            dialog.show()
        }
    }
    @JvmStatic
    fun judgeWelcome() {
        val allTips = info.scenes.map { WelcomeList[it] }.distinct().toList()
        val groups = allTips.groupBy { ConditionRegistry[it.conditionID] }
        val cond2Welcome = HashMap<Condition, ArrayList<WelcomeTip>>()
        for ((cond, tips) in groups.entries) {
            for (tip in tips) {
                if (cond.canShow(tip))
                    cond2Welcome.computeIfAbsent(cond) {
                        ArrayList()
                    }.add(tip)
            }
        }
        val conditionCanShow = cond2Welcome.maxByOrNull {
            it.key.priority
        }
        conditionCanShow?.let {
            val matches = it.value
            if (matches.isNotEmpty()) {
                it.key.applyShow(entity, matches)
                showWelcome = true
            }
        }
        entity.tip = WelcomeList["404NotFound"]
    }
    @JvmStatic
    fun modifierModInfo() {
        val meta = CioMod.Info.meta
        meta.displayName = "[#${R.C.Holo}]${meta.displayName}[]"
        Events.run(Trigger.update) {
            if (Time.time % 60 < 1f) {
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
        }
        CioVersion = Meta.Version
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
        //To load all templates and actions
        Templates
        Actions
        Conditions
    }
    @JvmStatic
    fun loadBundle() {
        bundle.loadMoreFrom("welcomes")
        if (Core.settings.getString("locale") != "en") {
            bundle.linkParent("welcomes")
        }
    }

    lateinit var infoJson: ObjectMap<String, JsonValue>
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun loadInfo() {
        val json = Res("WelcomeInfo.json").readAllText()
        infoJson = JsonIO.json.fromJson(ObjectMap::class.java, json) as ObjectMap<String, JsonValue>
        val curInfo = infoJson.get(Meta.Version)
        assert(curInfo != null) { "The welcome words information of Cyber IO ${Meta.Version} not found." }
        val default = curInfo.get("Default")?.asString() ?: "Default"
        val scenes = curInfo.get("Scene")?.asStringArray() ?: emptyArray()
        val parent: String? = curInfo.get("Parent")?.asString()
        info.default = default
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
        info.scenes = allScenes.toList()
    }

    class Info {
        var default = "Default"
        var scenes: List<String> = emptyList()
        val sceneSize: Int
            get() = scenes.size

        operator fun get(index: Int) =
            if (index !in scenes.indices)
                default
            else
                scenes[index]

        fun indexOf(tipID: String): Int =
            scenes.indexOf(tipID)

        val defaultIndex: Int
            get() = indexOf(default)
    }

    class Entity(
        val bundle: ReferBundleWrapper,
        val info: Info,
    ) {
        var tip: WelcomeTip = WelcomeTip.Default
        operator fun get(key: String) =
            bundle["$tip.$key"]

        val content: String
            get() = bundle["$tip"]

        fun content(vararg args: Any): String =
            bundle.format("$tip", *args)

        val icon: TR
            get() = tip.iconPath.Cio.atlas()
    }
}
