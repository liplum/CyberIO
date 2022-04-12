package net.liplum

import arc.Core.settings
import arc.Events
import arc.math.Mathf
import arc.scene.ui.Label
import arc.struct.ObjectMap
import arc.util.I18NBundle
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
    @JvmField val DefaultIconPath = R.Welcome.Atlas("cyber-io")
    @JvmField var welcomeInfo = WelcomeInfo()
    @JvmField var welcomeEntity = WelcomeEntity(welcomeInfo)
    @JvmField var bundle: I18NBundle = createModBundle()
    @JvmStatic
    fun showWelcomeDialog() {
        checkLastVersion()
        if (shouldShowWelcome) {
            val clickedTimes = settings.getInt(Setting.ClickWelcomeTimes, 0)
            // If it's the first time to play this version, let's show up the Zero Welcome.
            // Otherwise, roll until the result isn't as last one.
            val lastOne = settings.getInt(Setting.LastWelcome, 0)
            if (clickedTimes > 0) {
                welcomeEntity.randomize(lastOne)
                settings.put(Setting.LastWelcome, welcomeEntity.number)
            }
            welcomeEntity.genHead()
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
            val newsLabel = Label(welcomeEntity.content).apply {
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
    @JvmStatic
    fun loadBundle() {
        bundle.loadMoreFrom("welcomes")
    }
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun loadInfo() {
        val json = Res("WelcomeInfo.json").readAllText()
        val jsonObj = JsonIO.json.fromJson(ObjectMap::class.java, json) as ObjectMap<String, JsonValue>
        val info = jsonObj.get(Meta.Version)
        val variants = info.getInt(R.Welcome.WelcomeVariants, 0)
        welcomeInfo.variants = variants.coerceAtLeast(0)
        for (num in 1..variants) {
            welcomeInfo.number2Entry[num] = EntryWrapper(info.getChild(num.toString()))
        }
        welcomeInfo.iconPath = info.getString(R.Welcome.IconPath, DefaultIconPath)
    }

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
        var number = 0
        var head = ""
        fun randomize(avoid: Int) {
            val variants = info.variants
            if (variants <= 0) {
                number = 0
            } else {
                do {
                    number = Mathf.random(0, variants)
                } while (number == avoid)
            }
        }

        fun genHead() {
            head = "${Meta.Version}-$number"
        }

        val title: String
            get() = bundle["$head.title"].handleBundleRefer()
        val content: String
            get() = bundle[head].handleBundleRefer()
        val read: String
            get() = bundle["$head.read"].handleBundleRefer()
        val icon: TR
            get() = info.iconPath.atlas()
        val welcome: String
            get() = "welcome".bundle(bundle, Meta.Version)
    }
}
