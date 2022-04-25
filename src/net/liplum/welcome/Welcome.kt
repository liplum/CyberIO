package net.liplum.welcome

import arc.Core
import arc.Events
import arc.math.Mathf
import arc.scene.ui.Dialog
import arc.scene.ui.Label
import arc.struct.ObjectMap
import arc.util.Align
import arc.util.I18NBundle
import arc.util.Time
import arc.util.serialization.JsonValue
import mindustry.Vars
import mindustry.game.EventType.Trigger
import mindustry.io.JsonIO
import mindustry.ui.dialogs.BaseDialog
import net.liplum.*
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
    @JvmField var bundle: I18NBundle = createModBundle()
    @JvmStatic
    fun showWelcomeDialog() {
        checkLastVersion()
        if (ShowUpdate && Updater.requireUpdate) {
            Entity.sp = Entity.sp.setUpdate()
            updateDialog.show()
        } else {
            if (ShouldShowWelcome) {
                // If it's the first time to play this version, let's show up the Zero Welcome.
                // Otherwise, roll until the result isn't as last one.
                if (ClickWelcomeTimes > 0) {
                    Entity.randomize(LastWelcome)
                    LastWelcome = Entity.number
                }
                handleDialogShow()
            }
        }
    }

    fun handleDialogShow() {
        when (Info[Entity.number]) {
            Info.community -> communityDialog.show()
            else -> dialog.show()
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
    fun checkLastVersion() {
        val lastVersion = CioVersion
        if (lastVersion != Meta.Version) {
            ShouldShowWelcome = true
            ClickWelcomeTimes = 0
            ShowUpdate = true
            CioVersion = Meta.Version
            Entity.sp = Entity.sp.setInitial()
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
        val default = info.get("Default").asString()
        val scenes = info.get("Scene").asStringArray()
        val update = info.get("Update").asString()
        val community = info.get("Community").asString()
        Info.default = default
        Info.scenes = scenes
        Info.update = update
        Info.community = community
    }

    val dialog: BaseDialog by lazy {
        BaseDialog(Entity.title).apply {
            cont.image(Entity.icon)
                .maxSize(200f).pad(20f).row()
            cont.add(Label(Entity.welcome).apply {
                setAlignment(0)
                setWrap(true)
            }).growX()
                .row()
            cont.add(Label(Entity.content).apply {
                setAlignment(0)
                setWrap(true)
            }).growX()
                .row()
            cont.button(Entity.read) {
                recordClick()
                hide()
            }.size(200f, 50f)
        }
    }
    val updateDialog: BaseDialog by lazy {
        BaseDialog(Entity.title).apply {
            cont.image(Entity.icon)
                .maxSize(200f).pad(20f).row()
            cont.add(Label(Entity.genUpdate(Updater.latestVersion)).apply {
                setAlignment(0)
                setWrap(true)
            }).growX()
                .row()
            cont.table {
                it.button(Entity.yes) {
                    if (CioMod.jarFile != null) {
                        var progress = 0f
                        val loading = Vars.ui.loadfrag
                        loading.show("@downloading")
                        loading.setProgress { progress }
                        // Cache tips because the update will replace codes and cause class not found exception.
                        val successTip = R.Ctrl.UpdateModSuccess.bundle(Updater.latestVersion)
                        Updater.updateSelfByReplace(onProgress = { p ->
                            progress = p
                        }, onSuccess = {
                            loading.hide()
                            Time.run(10f) {
                                Vars.ui.showInfoOnHidden(successTip) {
                                    Core.app.exit()
                                }
                            }
                        }, onFailed = { error ->
                            Core.app.post {
                                loading.hide()
                                Dialog("").apply {
                                    getCell(cont).growX()
                                    cont.margin(15f).add(
                                        R.Ctrl.UpdateModFailed.bundle(Updater.latestVersion, error)
                                    ).width(400f).wrap().get().setAlignment(Align.center, Align.center)
                                    buttons.button("@ok") {
                                        this.hide()
                                    }.size(110f, 50f).pad(4f)
                                    closeOnBack()
                                }.show()
                            }
                        })
                    } else {
                        Updater.updateSelfByBuiltIn()
                    }
                    hide()
                }.size(150f, 50f)
                it.button(Entity.no) {
                    hide()
                }.size(150f, 50f)
                it.button(Entity.dontShow) {
                    ShowUpdate = false
                    hide()
                }.size(150f, 50f)
            }.growX()
                .row()
        }
    }
    val communityDialog: BaseDialog by lazy {
        BaseDialog(Entity.title).apply {
            cont.image(Entity.icon)
                .maxSize(200f).pad(20f).row()
            cont.add(Label(Entity.content).apply {
                setAlignment(0)
                setWrap(true)
            }).growX()
                .row()
            cont.table {
                it.button(Entity.yes) {
                    Core.app.openURI(Entity.link)
                    hide()
                }.size(200f, 50f)
                it.button(Entity.no) {
                    hide()
                }.size(200f, 50f)
            }.growX()
                .row()
        }
    }

    object Info {
        var default = "Default"
        var update = "Default"
        var community = "Default"
        var scenes: Array<String> = emptyArray()
        val sceneSize: Int
            get() = scenes.size

        operator fun get(index: Int) =
            if (index < 0)
                default
            else
                scenes[index]
    }
    @JvmInline
    value class EntitySp(val value: Int = 0) {
        val isInitial: Boolean
            get() = value isOn 0
        val isUpdate: Boolean
            get() = value isOn 1
        val isCommunity: Boolean
            get() = value isOn 2

        fun setInitial() = EntitySp(value on 0)
        fun setUpdate() = EntitySp(value on 1)
        fun setCommunity() = EntitySp(value on 2)
    }

    object Entity {
        var number = -1
        var sp = EntitySp()
        fun randomize(avoid: Int) {
            val variants = Info.sceneSize
            if (variants <= 0) {
                number = 0
            } else {
                do {
                    number = Mathf.random(0, variants - 1)
                } while (number == avoid)
            }
        }

        val tip: WelcomeTip
            get() = if (sp.isUpdate) {
                WelcomeList[Info.update]
            } else if (sp.isInitial) {
                WelcomeList[Info.default]
            } else {
                WelcomeList[Info[number]]
            }
        val title: String
            get() = bundle["$tip.title"].handleBundleRefer()
        val content: String
            get() = bundle["$tip"].handleBundleRefer()
        val read: String
            get() = bundle["$tip.read"].handleBundleRefer()
        val icon: TR
            get() = tip.iconPath.Cio.atlas()
        val welcome: String
            get() = "welcome".bundle(bundle, Meta.Version)
        // For update
        fun genUpdate(version: Version2): String =
            bundle.format("$tip", version)

        val yes: String
            get() = bundle["$tip.yes"]
        val no: String
            get() = bundle["$tip.no"]
        val dontShow: String
            get() = bundle["$tip.dont-show"]
        // For community
        val link: String
            get() = bundle["$tip.link"]
    }
}
