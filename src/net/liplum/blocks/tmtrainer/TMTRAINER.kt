package net.liplum.blocks.tmtrainer

import arc.Core
import arc.graphics.g2d.Draw
import arc.util.Time
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.ItemTurret
import net.liplum.ClientOnly
import net.liplum.WhenNotPaused
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.animations.anims.AnimationObj
import net.liplum.lib.animations.anims.ITimeModifier
import net.liplum.draw
import net.liplum.math.PolarPos
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
        alternate = true
        inaccuracy = 1f
        rotateSpeed = 10f
    }

    override fun load() {
        super.load()
        CoreAnim = this.autoAnim("core", CoreAnimFrames, 60f)
        HeadTR = this.sub("head")
        EmptyCoreAnim = this.autoAnim("core-empty", CoreAnimFrames, 60f)
    }

    override fun icons() = arrayOf(
        baseRegion, HeadTR, region
    )

    open inner class TMTRAINERBUILD : ItemTurretBuild() {
        @ClientOnly lateinit var coreAnimObj: AnimationObj
        @ClientOnly lateinit var emptyCoreAnimObj: AnimationObj
        @ClientOnly var targetPol: PolarPos = PolarPos(headMax, 0f)
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

        override fun update() {
            super.update()
            val delta = if (wasShooting) delta() else -delta()
            virusCharge += delta / 2.5f
        }

        override fun draw() {
            WhenNotPaused {
                coreAnimObj.spend(Time.delta)
                emptyCoreAnimObj.spend(Time.delta)
            }
            Draw.rect(baseRegion, x, y)
            Draw.color()

            Draw.z(Layer.turret)
            WhenNotPaused {
                targetPol.a = rotation.radian
                var tpr = targetPol.r
                val delta = virusCharge * 0.001f
                tpr = if (wasShooting) tpr - delta else tpr + delta
                tpr = tpr.coerceIn(headMin, headMax)
                targetPol.r = tpr
            }

            tr2.trns(rotation, -recoil)
            val drawRotation = rotation.draw
            val drawX = x + tr2.x
            val drawY = y + tr2.y
            Draw.rect(
                HeadTR,
                drawX + targetPol.toX(),
                drawY + targetPol.toY(),
                drawRotation
            )

            Drawf.shadow(region, drawX - elevation, drawY - elevation, drawRotation)
            drawer(this)
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

            if (Core.atlas.isFound(heatRegion)) {
                heatDrawer(this)
            }
        }
    }
}