package net.liplum.ui

import arc.func.Floatp
import arc.scene.Element
import arc.scene.ui.layout.Table
import mindustry.Vars
import mindustry.gen.BlockUnitc
import mindustry.graphics.Pal
import net.liplum.S
import net.liplum.annotations.Only
import net.liplum.annotations.SubscribeEvent
import net.liplum.blocks.prism.AutoRGB
import net.liplum.blocks.prism.Prism
import net.liplum.events.CioInitEvent
import net.liplum.holo.HoloUnit
import net.liplum.common.utils.setF
import net.liplum.mdt.OverwriteVanilla
import net.liplum.mdt.safeCall
import net.liplum.mdt.ui.DynamicContentInfoDialog

@OverwriteVanilla
object OverwrittenUI {
    @JvmStatic
    @OverwriteVanilla
    @SubscribeEvent(CioInitEvent::class,Only.client)
    fun overwrite() {
        overwriteContentInfo()
        overwriteHud()
    }
    @OverwriteVanilla
    fun overwriteContentInfo() {
        safeCall { Vars.ui.content = DynamicContentInfoDialog(Vars.ui.content) }
    }
    @OverwriteVanilla("mindustry.ui.fragments.HudFragment#makeStatusTable")
    fun overwriteHud() {
        safeCall {
            val wavePanel = Vars.ui.hudGroup.find<Table>("waves")
            val sider = wavePanel.find<Element> {
                it.javaClass.name.contains("SideBar")
            }
            if (sider != null) {
                val table = sider.parent
                val siderBars = table.children.toList().filter { it.javaClass.name.contains("SideBar") }
                val siderBar = siderBars[1]
                siderBar.setF("amount", Floatp {
                    val player = Vars.player
                    if (player.dead())
                        0f
                    else {
                        val unit = player.unit()
                        if (player.displayAmmo())
                            unit.ammof()
                        else {
                            if (unit is HoloUnit)
                                unit.restLifePercent
                            else
                                unit.healthf()
                        }
                    }
                })
                siderBar.update {
                    val player = Vars.player
                    val unit = Vars.player.unit()
                    siderBar.color.set(
                        if (unit is HoloUnit)
                            S.Hologram
                        else if (player.displayAmmo())
                            if (player.dead() || unit is BlockUnitc) {
                                if (unit is BlockUnitc) {
                                    if (unit.tile() is Prism.PrismBuild)
                                        AutoRGB()
                                    else
                                        Pal.ammo
                                } else
                                    Pal.ammo
                            } else
                                unit.type.ammoType.color()
                        else
                            Pal.health
                    )
                }
            }
        }
    }
}