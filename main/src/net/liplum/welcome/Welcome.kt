package net.liplum.welcome

import arc.Core
import arc.Events
import arc.util.Time
import mindustry.game.EventType.Trigger
import net.liplum.CioMod
import net.liplum.Meta
import net.liplum.Settings.CioVersion
import net.liplum.Settings.ClickWelcomeTimes
import net.liplum.Settings.LastWelcomeID
import net.liplum.Settings.ShouldShowWelcome
import net.liplum.Settings.ShowUpdate
import net.liplum.Var
import net.liplum.annotations.Only
import net.liplum.annotations.SubscribeEvent
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.cio
import net.liplum.common.util.ReferBundleWrapper
import net.liplum.common.util.allMaxBy
import net.liplum.common.util.randomExcept
import net.liplum.event.CioInitEvent
import net.liplum.math.randomByWeights
import net.liplum.welcome.Welcome.handleTrRefer
import plumy.core.ClientOnly
import plumy.core.assets.TR
import plumy.dsl.sprite

@ClientOnly
object Welcome {
    var bundle = ReferBundleWrapper.create()
    private var version = WelcomeScenePacks.v5_1

    @JvmStatic
    @ClientOnly
    fun showWelcomeDialog() {
        checkLastVersion()
        judgeWelcome()?.showTip()
        //For debug
        /*val tip = WelcomeList.find { it.id == "SetOutErekir" }
        tip?.condition?.canShow(tip)*/
        //entity.showTipByID("SetOutErekir")
    }

    @JvmStatic
    fun judgeWelcome(): WelcomeEntity? {
        val allTips = version.scenes.distinct().toList()
        val tipsCanShow = allTips.filter { it.condition.canShow(it) }
        val allCandidates = tipsCanShow.allMaxBy { it.condition.priority(it) }
        if (allCandidates.isEmpty()) {
            return null
        }
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
            return createEntity(res)
        }
        return null
    }

    fun createEntity(scene: WelcomeScene): WelcomeEntity {
        return WelcomeEntity(bundle, version, scene)
    }

    @JvmStatic
    @SubscribeEvent(CioInitEvent::class, Only.client)
    fun modifierModInfo() {
        val meta = CioMod.Info.meta
        meta.displayName = "[#${Var.Hologram}]${Meta.Name}[]"
        Events.run(Trigger.update) {
            if (Time.time % 60 < 1f) {
                meta.author = RandomName.randomTinted(Meta.Author)
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
        //To load all templates and actions
        WelcomeTemplates
        WelcomeActions
        WelcomeConditions
    }

    @JvmStatic
    fun loadBundle() {
        bundle.loadMoreFrom("welcomes")
        if (Core.settings.getString("locale") != "en") {
            bundle.linkParent("welcomes")
        }
    }

    fun String.handleTrRefer(): TR =
        if (startsWith('@'))
            removePrefix("@").sprite
        else cio.sprite
}

class WelcomeEntity(
    val bundle: ReferBundleWrapper,
    val info: WelcomeScenePack,
    val scene: WelcomeScene,
) {
    operator fun get(key: String) =
        bundle["$scene.$key"]

    val content: String
        get() = bundle["$scene"]

    fun content(vararg args: Any): String =
        bundle.format("$scene", *args)

    val icon: TR
        get() = scene.iconPath.handleTrRefer()

    fun showTip() {
        scene.template.gen(this).show()
    }
}
