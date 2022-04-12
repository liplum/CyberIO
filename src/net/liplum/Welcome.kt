package net.liplum

import arc.Core
import arc.Core.settings
import arc.Events
import arc.math.Mathf
import arc.scene.ui.Label
import arc.struct.ObjectMap
import arc.util.Time
import arc.util.serialization.JsonValue
import mindustry.game.EventType.Trigger
import mindustry.io.JsonIO
import mindustry.ui.dialogs.BaseDialog
import net.liplum.R.Setting
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.lib.Res
import net.liplum.utils.*

@ClientOnly
object Welcome {
    var welcomeInfo = WelcomeInfo()
    var welcomeEntity = WelcomeEntity(welcomeInfo)
    @JvmStatic
    fun showWelcomeDialog() {
        checkLastVersion()
        if (shouldShowWelcome) {
            welcomeEntity.randomize()
            dialog.show()
        }
    }
    @JvmStatic
    fun checkLastVersion() {
        val lastVersion = settings.getString(Setting.Version, Meta.Version)
        if (lastVersion != Meta.Version) {
            settings.put(Setting.ShowWelcome, true)
            settings.put(Setting.Version, Meta.Version)
            settings.put(Setting.ClickWelcomeTimes, 0)
        }
    }
    @JvmStatic
    fun recordClick() {
        val formerTimes = settings.getInt(Setting.ClickWelcomeTimes, 0)
        settings.put(Setting.ClickWelcomeTimes, formerTimes + 1)
    }

    val shouldShowWelcome: Boolean
        get() {
            val showWelcome = settings.getBool(Setting.ShowWelcome, true)
            return showWelcome
        }
    val dialog: BaseDialog by lazy {
        BaseDialog(welcomeEntity.title).apply {
            cont.image(welcomeEntity.icon)
                .maxSize(200f).pad(20f).row()
            val welcomeLabel = Label(welcomeEntity.welcome).apply {
                setAlignment(0)
                setWrap(true)
            }
            cont.add(welcomeLabel)
                .growX()
                .row()
            val newsLabel = Label(welcomeEntity.news).apply {
                setAlignment(0)
                setWrap(true)
            }
            cont.add(newsLabel)
                .growX()
                .row()
            cont.button(welcomeEntity.read) {
                recordClick()
                hide()
            }.size(200f, 50f)
            layout()
        }
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
    fun load() {
        loadBundle()
        loadInfo()
    }

    fun loadBundle() {
        val locale = settings.getString("locale")
        Res("welcomes/$locale.properties").tryLoad {
            Core.bundle.loadMore(reader())
        }.whenNotFound {
            Core.bundle.loadMore(Res("welcomes/${Meta.DefaultLang}.properties").reader())
        }
    }
    @Suppress("UNCHECKED_CAST")
    fun loadInfo() {
        val json = Res("WelcomeInfo.json").readAllText()
        val jsonObj = JsonIO.json.fromJson(ObjectMap::class.java, json) as ObjectMap<String, JsonValue>
        val info = jsonObj.get(Meta.Version)
        val variants = info.getInt(R.Welcome.WelcomeVariants, 0)
        welcomeInfo.variants = variants.coerceAtLeast(0)
        for (num in 1..variants) {
            welcomeInfo.number2Entry[num] = EntryWrapper(info.getChild(num.toString()))
        }
    }
}

private val DefaultIconPath = R.Welcome.Atlas("cyber-io")

class WelcomeInfo {
    var iconPath = DefaultIconPath
    var variants: Int = 0
    var number2Entry: MutableMap<Int, EntryWrapper> = HashMap()
}

class EntryWrapper(val jValue: JsonValue) {
    val iconPath: String
        get() = jValue.getString("IconPath", DefaultIconPath)
}

class WelcomeEntity(val info: WelcomeInfo) {
    var head = ""
    fun randomize() {
        val variants = info.variants
        val number = if (variants <= 0)
            0
        else
            Mathf.random(0, variants + 1)
        head = "${R.Welcome.Gen(Meta.Version)}.$number"
    }

    val title: String
        get() = "$head.title".bundle.handleBundleRefer()
    val content: String
        get() = head.bundle.handleBundleRefer()
    val read: String
        get() = "$head.read".bundle.handleBundleRefer()
    val icon: TR
        get() = info.iconPath.atlas()
    val welcome: String
        get() = R.Welcome.Gen("welcome").bundle(Meta.Version)
    val news: String
        get() = R.Welcome.Gen("news").bundle(content)
}