package net.liplum

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
import net.liplum.Settings.CioVersion
import net.liplum.Settings.ClickWelcomeTimes
import net.liplum.Settings.LastWelcome
import net.liplum.Settings.ShouldShowWelcome
import net.liplum.Settings.ShowUpdate
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.lib.Res
import net.liplum.update.Updater
import net.liplum.update.Version2
import net.liplum.utils.*

@ClientOnly
object Welcome {
    const val DefaultIconPath = "welcome-cyber-io"
    @JvmField var welcomeInfo = WelcomeInfo()
    @JvmField var welcomeEntity = WelcomeEntity(welcomeInfo)
    @JvmField var bundle: I18NBundle = createModBundle()
    @JvmStatic
    fun showWelcomeDialog() {
        checkLastVersion()
        if (ShowUpdate && Updater.requireUpdate) {
            updateDialog.show()
        } else {
            if (ShouldShowWelcome) {
                // If it's the first time to play this version, let's show up the Zero Welcome.
                // Otherwise, roll until the result isn't as last one.
                if (ClickWelcomeTimes > 0) {
                    welcomeEntity.randomize(LastWelcome)
                    LastWelcome = welcomeEntity.number
                }
                welcomeEntity.genHead()
                dialog.show()
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

    val dialog: BaseDialog by lazy {
        BaseDialog(welcomeEntity.title).apply {
            cont.image(welcomeEntity.icon)
                .maxSize(200f).pad(20f).row()
            cont.add(Label(welcomeEntity.welcome).apply {
                setAlignment(0)
                setWrap(true)
            }).growX()
                .row()
            cont.add(Label(welcomeEntity.content).apply {
                setAlignment(0)
                setWrap(true)
            }).growX()
                .row()
            cont.button(welcomeEntity.read) {
                recordClick()
                hide()
            }.size(200f, 50f)
        }
    }
    val updateDialog: BaseDialog by lazy {
        BaseDialog(welcomeEntity.updateTitle).apply {
            cont.image(welcomeEntity.icon)
                .maxSize(200f).pad(20f).row()
            cont.add(Label(welcomeEntity.genUpdate(Updater.latestVersion)).apply {
                setAlignment(0)
                setWrap(true)
            }).growX()
                .row()
            cont.table {
                it.button(welcomeEntity.updateYes) {
                    Updater.updateSelf()
                    hide()
                }.size(150f, 50f)
                it.button(welcomeEntity.updateNo) {
                    hide()
                }.size(150f, 50f)
                it.button(welcomeEntity.updateDontShow) {
                    ShowUpdate = false
                    hide()
                }.size(150f, 50f)
            }.growX()
                .row()
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
    @ClientOnly
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
        for (num in 0..variants) {
            welcomeInfo.number2Entry[num] = EntryWrapper(info.getChild("$num")?.parent)
        }
    }

    class WelcomeInfo {
        var variants: Int = 0
        var number2Entry: MutableMap<Int, EntryWrapper> = HashMap()
        operator fun get(index: Int): EntryWrapper =
            number2Entry[index]!!
    }

    class EntryWrapper(val jValue: JsonValue?) {
        val iconPath: String
            get() = jValue?.getString(R.Welcome.IconPath, DefaultIconPath) ?: DefaultIconPath
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
            get() = info[number].iconPath.Cio.atlas()
        val welcome: String
            get() = "welcome".bundle(bundle, Meta.Version)
        val updateHead: String
            get() = "${Meta.Version}.update"

        fun genUpdate(version: Version2): String =
            bundle.format(updateHead, version)

        val updateTitle: String
            get() = bundle["$updateHead.title"]
        val updateYes: String
            get() = bundle["$updateHead.update"]
        val updateNo: String
            get() = bundle["$updateHead.no"]
        val updateDontShow: String
            get() = bundle["$updateHead.dont-show"]
    }
}
