package net.liplum.heimdall

import arc.scene.ui.Image
import arc.scene.ui.Label
import arc.scene.ui.ScrollPane
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Table
import mindustry.content.Items
import mindustry.ctype.UnlockableContent
import mindustry.gen.Tex
import mindustry.graphics.Pal
import plumy.core.assets.TR
import net.liplum.common.TimeH
import net.liplum.common.ui.addProgressTable
import net.liplum.common.util.IBundlable
import net.liplum.ui.addSeparatorLine

class MiningModule : IBundlable {
    var curSector = Sector.X
    lateinit var op: HeimdallOp
    fun build(t: Table) {
        t.add(ScrollPane(Table().apply {
            val cate2Minerals = curSector.resources.groupBy { it.meta.category }
            for ((cate, minerals) in cate2Minerals) {
                add(bundle("category.${cate.name}")).left().pad(10f).row()
                addSeparatorLine(Pal.accent)
                for (mineral in minerals) {
                    val totalTimeText = TimeH.toTimeFullString(mineral.timeReq)
                    addProgressTable {
                        var miningProgress = 0f
                        progress = { miningProgress }
                        progressOmit = 0.01f
                        progressColor.set(Pal.bar).a(0.5f)
                        background(Tex.barTop)
                        add(Image(mineral.meta.icon)).pad(5f).left()
                        add(Label(bundle("reserve.${mineral.reserve.name}"))).pad(5f).growX().left()
                        val button = TextButton("").apply {
                            changed {
                                op.addMining(mineral.id, mineral.timeReq)
                            }
                        }
                        add(button).width(150f)
                        update {
                            val task = op.getMiningTaskByID(mineral.id)
                            if (task != null) {
                                miningProgress = task.progress
                                button.setText(TimeH.toTimeFullString(task.restTime))
                                button.isDisabled = true
                            } else {
                                miningProgress = 0f
                                button.setText(totalTimeText)
                                button.isDisabled = false
                            }
                        }
                    }.growX().row()
                }
            }
        }.apply {
            top()
        }).apply {
            setFadeScrollBars(true)
        }).grow()
    }

    override val bundlePrefix = "mining"
    override val parentBundle = HeimdallProjectGame
}

enum class ResourceReserve(
    val times: Int,
) {
    High(10), Normal(5), Low(3), Poor(1);
}

enum class ResourceCategory {
    Mineral, Liquid
}

class ResourceMeta {
    var id = 0
    var content: UnlockableContent = Items.copper
    var category = ResourceCategory.Mineral
    var icon = TR()

    companion object {
        val X = ResourceMeta()
    }
}

class Resource {
    var id: ResourceID = 0
    var meta = ResourceMeta.X
    var reserve = ResourceReserve.Poor
    /** Unit: second */
    var timeReq = TimeH.toSec(min = 1)
    override fun toString() =
        "[${meta.category}#$id]${meta.content.localizedName}($reserve->$timeReq)"
}