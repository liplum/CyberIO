package net.liplum.heimdall

import arc.scene.ui.Image
import arc.scene.ui.layout.Table
import mindustry.gen.Tex
import mindustry.graphics.Pal
import mindustry.ui.Bar
import net.liplum.inCio
import net.liplum.lib.ui.INavigable
import net.liplum.lib.ui.TRD
import net.liplum.lib.ui.UIToast
import net.liplum.lib.ui.addTable
import net.liplum.lib.utils.Bundlable
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.ui.tabview.TabItem
import net.liplum.mdt.ui.tabview.TabView

@ClientOnly
object HeimdallProjectGame : Bundlable {
    val data = HeimdallData()
    val op = HeimdallOp(data)
    var mining = MiningModule()
    var resourceLoaded = false
    var resourceInited = false
    var curSector = Sector.X
        set(value) {
            field = value
            mining.curSector = value
        }
    lateinit var hudTable: Table
    fun build(cont: Table): INavigable {
        tryLoad()
        tryInit()
        cont.addTable {
            hudTable = this
            buildHud(this)
        }.left().growX().row()
        cont.addTable {
            allTabs.resetItem()
            allTabs.build(this)
        }.grow()
        return allTabs
    }

    fun buildHud(t: Table) {
        t.clear()
        t.addTable {
            add(Image(TRD("heimdall".inCio))).left()
            addTable {
                addBar(Bar({ data.hud.health.toString() },
                    { Pal.health },
                    { data.hud.progress }))
            }.top().growX().pad(10f)
        }.left().growX().pad(10f)
        t.addTable {
            background(Tex.button)
            add(curSector.planet.localizedName).row()
            add(curSector.localizedName).row()
        }.right()
    }

    fun tryInit() {
        if (!resourceInited) {
            curSector = HeimdallVars.planets.first().sectors.last()
            mining.op = op
            resourceInited = true
        }
    }

    fun tryLoad() {
        if (!resourceLoaded) {
            HeimdallVars.load()
            resourceLoaded = true
        }
    }

    fun Table.addBar(bar: Bar) {
        addTable {
            add(bar).grow().row()
        }.grow().minHeight(18f).width(150f).left().pad(4f).row()
    }

    val allTabs = TabView().apply view@{
        addTab(TabItem("Mining").apply {
            buildIcon = {
                add("Mining").row()
            }
            buildContent {
                mining.build(this)
                return@buildContent this@view
            }
        })
    }
    override val bundlePrefix = "heimdall"
    var toastUI = UIToast().apply {
        background = Tex.button
    }
    val title: String
        get() = bundle("title")
}