package net.liplum.heimdall

import arc.scene.ui.Image
import arc.scene.ui.layout.Table
import mindustry.gen.Tex
import mindustry.graphics.Pal
import mindustry.ui.Bar
import net.liplum.ui.INavigable
import net.liplum.common.ui.UIToast
import net.liplum.common.util.IBundlable
import net.liplum.cioTR
import plumy.core.ClientOnly
import net.liplum.ui.CyberIOMenu
import net.liplum.ui.control.TabItem
import net.liplum.ui.control.TabView
import net.liplum.ui.TRD
import net.liplum.ui.addTable

@ClientOnly
object HeimdallProjectGame : IBundlable {
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
            add(Image(TRD("heimdall".cioTR))).left()
            addTable {
                addBar(
                    Bar({ data.hud.health.toString() },
                        { Pal.health },
                        { data.hud.progress })
                )
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

    val allTabs = TabView(CyberIOMenu.tabViewStyle).apply view@{
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