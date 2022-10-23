package net.liplum.ui

import arc.Core
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.Table
import arc.util.Scaling
import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.gen.Iconc
import mindustry.graphics.Pal
import mindustry.ui.dialogs.ContentInfoDialog
import mindustry.world.meta.Stats
import net.liplum.utils.OverwriteVanilla

@OverwriteVanilla
class DynamicContentInfoDialog(
    val delegate: ContentInfoDialog? = null,
) : ContentInfoDialog() {
    companion object {
        val dynamicInfo = HashSet<UnlockableContent>()
        fun <T : UnlockableContent> T.registerDynamicInfo(): T {
            dynamicInfo.add(this)
            return this
        }
    }

    override fun show(content: UnlockableContent) {
        if (dynamicInfo.contains(content) || delegate == null) {
            showDynamic(content)
        } else {
            delegate.show(content)
        }
    }

    fun showDynamic(content: UnlockableContent) {
        cont.clear()
        val table = Table()
        table.margin(10f)
        //initialize stats if they haven't been yet
        content.checkStats()

        table.table { title1: Table ->
            title1.image(content.uiIcon).size(Vars.iconXLarge).scaling(Scaling.fit)
            title1.label { "[accent]" + content.localizedName + if (Core.settings.getBool("console")) "\n[gray]${content.name}" else "" }
                .padLeft(5f)
        }

        table.row()

        if (content.description != null) {
            val any: Boolean = content.stats.toMap().size > 0
            if (any) {
                table.add("@category.purpose").color(Pal.accent).fillX().padTop(10f)
                table.row()
            }
            table.label { "[lightgray]" + content.displayDescription() }.wrap().fillX().padLeft(if (any) 10f else 0.toFloat()).width(500f)
                .padTop(if (any) 0f else 10.toFloat()).left()
            table.row()
            if (!content.stats.useCategories && any) {
                table.add("@category.general").fillX().color(Pal.accent)
                table.row()
            }
        }
        val stats: Stats = content.stats

        for (cat in stats.toMap().keys()) {
            val map = stats.toMap()[cat]
            if (map.size == 0) continue
            if (stats.useCategories) {
                table.add("@category." + cat.name).color(Pal.accent).fillX()
                table.row()
            }
            for (stat in map.keys()) {
                table.table { inset: Table ->
                    inset.left()
                    inset.add("[lightgray]" + stat.localized() + ":[] ").left().top()
                    val arr = map[stat]
                    for (value in arr) {
                        value.display(inset)
                        inset.add().size(10f)
                    }
                }.fillX().padLeft(10f)
                table.row()
            }
        }

        if (content.details != null) {
            table.add("[gray]" + if (content.unlocked() || !content.hideDetails) content.details else Iconc.lock.toString() + " " + Core.bundle["unlock.incampaign"])
                .pad(6f).padTop(20f).width(400f).wrap().fillX()
            table.row()
        }

        content.displayExtra(table)
        val pane = ScrollPane(table)
        cont.add(pane)
        show()
    }
}