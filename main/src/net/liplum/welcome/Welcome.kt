package net.liplum.welcome

import arc.Core
import arc.Events
import arc.struct.ObjectMap
import arc.util.Time
import arc.util.serialization.JsonValue
import mindustry.game.EventType.Trigger
import mindustry.io.JsonIO
import net.liplum.Cio
import net.liplum.CioMod
import net.liplum.Meta
import net.liplum.S
import net.liplum.Settings.CioVersion
import net.liplum.Settings.ClickWelcomeTimes
import net.liplum.Settings.LastWelcomeID
import net.liplum.Settings.ShouldShowWelcome
import net.liplum.Settings.ShowUpdate
import net.liplum.annotations.Only
import net.liplum.annotations.SubscribeEvent
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.events.CioInitEvent
import net.liplum.lib.Res
import net.liplum.lib.TR
import net.liplum.lib.math.randomByWeights
import net.liplum.lib.utils.ReferBundleWrapper
import net.liplum.lib.utils.allMaxBy
import net.liplum.lib.utils.randomExcept
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.atlas

@ClientOnly
object Welcome {
    var bundle = ReferBundleWrapper.create()
    private var info = Info()
    fun genEntity() = Entity(bundle, info)
    private var entity = genEntity()
    private var showWelcome = false
    @JvmStatic
    @ClientOnly
    fun showWelcomeDialog() {
        checkLastVersion()
        judgeWelcome()
        if (showWelcome) {
            entity.showTip()
        }
        //For debug
        //val tip = WelcomeList.find { it.id == "SetOutErekir" }
        //tip?.condition?.canShow(tip)
        //entity.showTipByID("AdBlock")
    }
    @JvmStatic
    fun judgeWelcome() {
        val allTips = info.scenes.map { WelcomeList[it] }.distinct().toList()
        val tipsCanShow = allTips.filter { it.condition.canShow(it) }
        val allCandidates = tipsCanShow.allMaxBy { it.condition.priority(it) }
        var sumChance = 0
        val weights = IntArray(allCandidates.size) {
            val chance = allCandidates[it].chance
            sumChance += chance
            chance
        }
        val res = allCandidates.randomExcept(
            atLeastOne = true,
            random = {
                this.randomByWeights(weights, sumChance)
            }
        ) {
            id == LastWelcomeID
        }
        if (res != null) {
            LastWelcomeID = res.id
            entity.tip = res
            showWelcome = true
        }
    }
    @JvmStatic
    @SubscribeEvent(CioInitEvent::class, Only.client)
    fun modifierModInfo() {
        val meta = CioMod.Info.meta
        meta.displayName = "[#${S.Hologram}]${Meta.Name}[]"
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
            LastWelcomeID = ""
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

    fun String.handleTrRefer(): TR =
        if (startsWith('@'))
            removePrefix("@").atlas()
        else Cio.atlas()

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
            get() = tip.iconPath.handleTrRefer()

        fun showTip() {
            tip.template.gen(this).show()
        }

        companion object {
            fun Entity.showTipByID(id: String): Entity {
                tip = WelcomeList[id]
                showTip()
                return this
            }
        }
    }
}
