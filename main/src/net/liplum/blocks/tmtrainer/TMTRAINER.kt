package net.liplum.blocks.tmtrainer

import arc.Core
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.scene.ui.Image
import arc.scene.ui.layout.Table
import arc.struct.Bits
import arc.util.Strings
import arc.util.Time
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.type.Category
import mindustry.world.blocks.defense.turrets.ItemTurret
import mindustry.world.draw.DrawBlock
import mindustry.world.draw.DrawMulti
import mindustry.world.draw.DrawRegion
import mindustry.world.meta.StatUnit
import net.liplum.ClientOnly
import net.liplum.WhenNotPaused
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.animations.anims.AnimationObj
import net.liplum.lib.animations.anims.ITimeModifier
import net.liplum.lib.mixin.drawRotation
import net.liplum.lib.mixin.drawX
import net.liplum.lib.mixin.drawY
import net.liplum.math.Polar
import net.liplum.utils.*

open class TMTRAINER(name: String) : ItemTurret(name) {
    @ClientOnly lateinit var CoreAnim: Animation
    @ClientOnly lateinit var EmptyCoreAnim: Animation
    @ClientOnly lateinit var HeadTR: TR
    @JvmField @ClientOnly var headMax = 0.45f
    @JvmField @ClientOnly var headMin = -3f
    @JvmField var CoreAnimFrames = 8
    @JvmField var maxVirusChargeSpeedUp = 2.5f

    init {
        drawer = DrawMulti(
            DrawRegion("-base"),
            object : DrawRegion("-head") {
                override fun draw(build: Building) = (build as TMTRAINERBUILD).run {
                    WhenNotPaused {
                        targetPol.a = rotation.radian
                        var tpr = targetPol.r
                        val delta = virusCharge * 0.001f
                        tpr = if (wasShooting) tpr - delta else tpr + delta
                        tpr = tpr.coerceIn(headMin, headMax)
                        targetPol.r = tpr
                    }
                    Draw.rect(region, drawX + targetPol.x, drawY + targetPol.y, drawRotation)
                }
            },
            object : DrawRegion("") {
                override fun draw(build: Building) = (build as TMTRAINERBUILD).run {
                    Drawf.shadow(region, drawX - elevation, drawY - elevation, drawRotation)
                    Draw.rect(region, drawX, drawY, drawRotation)
                }
            },
            object : DrawBlock() {
                override fun draw(build: Building) = (build as TMTRAINERBUILD).run {
                    emptyCoreAnimObj.draw(
                        drawX,
                        drawY,
                        drawRotation
                    )
                    if (unit.ammo() > 0) {
                        Draw.alpha(unit.ammof())
                        coreAnimObj.draw(
                            drawX,
                            drawY,
                            drawRotation
                        )
                        Draw.color()
                    }
                }
            }
        )
    }

    override fun load() {
        super.load()
        CoreAnim = autoAnim("core", CoreAnimFrames, 60f)
        EmptyCoreAnim = autoAnim("core-empty", CoreAnimFrames, 60f)
    }

    override fun icons() =
        arrayOf(this.sub("base"), this.selfTR(),this.sub("head"))

    open inner class TMTRAINERBUILD : ItemTurretBuild() {
        @ClientOnly lateinit var coreAnimObj: AnimationObj
        @ClientOnly lateinit var emptyCoreAnimObj: AnimationObj
        @ClientOnly var targetPol: Polar =
            Polar(headMax, 0f)
        open var virusCharge = 0f
            set(value) {
                field = value.coerceIn(0f, 60f)
            }

        init {
            ClientOnly {
                val boost = ITimeModifier {
                    if (unit.ammo() > 0)
                        it / this.timeScale * (1f + virusCharge / 10f) * unit.ammof()
                    else 0f
                }
                coreAnimObj = CoreAnim.gen().tmod(boost)
                emptyCoreAnimObj = EmptyCoreAnim.gen().tmod(boost)
            }
        }

        override fun draw() {
            WhenNotPaused {
                coreAnimObj.spend(Time.delta)
                emptyCoreAnimObj.spend(Time.delta)
            }
            super.draw()
        }

        override fun display(table: Table) {
            table.table { t: Table ->
                t.left()
                t.add(Image(block.getDisplayIcon(tile))).size((8 * 4).toFloat())
                t.labelWrap { block.getDisplayName(tile) }.left().width(190.0f).padLeft(5f)
            }.growX().left()
            table.row()
            if (team == Vars.player.team()) {
                table.table { bars: Table ->
                    bars.defaults().growX().height(18.0f).pad(4f)
                    displayBars(bars)
                }.growX()
                table.row()
                table.table { displayConsumption(it) }.growX()
                val displayFlow = (block.category == Category.distribution || block.category == Category.liquid) && block.displayFlow
                if (displayFlow) {
                    val ps = " " + StatUnit.perSecond.localized()
                    val flowItems = flowItems()
                    if (flowItems != null) {
                        table.row()
                        table.left()
                        table.table { l: Table ->
                            val current = Bits()
                            val rebuild = Runnable {
                                l.clearChildren()
                                l.left()
                                for (item in Vars.content.items()) {
                                    if (flowItems.hasFlowItem(item)) {
                                        l.image(item.uiIcon).padRight(3.0f)
                                        l.label {
                                            if (flowItems.getFlowRate(item) < 0) "..." else Strings.fixed(flowItems.getFlowRate(item),
                                                1) + ps
                                        }.color(Color.lightGray)
                                        l.row()
                                    }
                                }
                            }
                            rebuild.run()
                            l.update {
                                for (item in Vars.content.items()) {
                                    if (flowItems.hasFlowItem(item) && !current[item.id.toInt()]) {
                                        current.set(item.id.toInt())
                                        rebuild.run()
                                    }
                                }
                            }
                        }.left()
                    }
                    if (liquids != null) {
                        table.row()
                        table.left()
                        table.table { l: Table ->
                            val current = Bits()
                            val rebuild = Runnable {
                                l.clearChildren()
                                l.left()
                                for (liquid in Vars.content.liquids()) {
                                    if (liquids.hasFlowLiquid(liquid)) {
                                        l.image(liquid.uiIcon).padRight(3.0f)
                                        l.label {
                                            if (liquids.getFlowRate(liquid) < 0) "..." else Strings.fixed(liquids.getFlowRate(liquid),
                                                1) + ps
                                        }.color(Color.lightGray)
                                        l.row()
                                    }
                                }
                            }
                            rebuild.run()
                            l.update {
                                for (liquid in Vars.content.liquids()) {
                                    if (liquids.hasFlowLiquid(liquid) && !current[liquid.id.toInt()]) {
                                        current.set(liquid.id.toInt())
                                        rebuild.run()
                                    }
                                }
                            }
                        }.left()
                    }
                }
                if (Vars.net.active() && lastAccessed != null) {
                    table.row()
                    table.add(Core.bundle.format("lastaccessed", lastAccessed)).growX().wrap().left()
                }
                table.marginBottom(-5f)
            }
        }

        override fun update() {
            super.update()
            val delta = if (wasShooting) delta() else -delta()
            virusCharge += delta / 2.5f
        }
    }
}