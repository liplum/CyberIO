package net.liplum.blocks.tmtrainer

import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.util.Time
import mindustry.graphics.Drawf
import mindustry.world.Block
import mindustry.world.blocks.defense.turrets.ItemTurret
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.draw.DrawTurret
import net.liplum.ClientOnly
import net.liplum.WhenNotPaused
import net.liplum.draw
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.animations.anims.AnimationObj
import net.liplum.lib.animations.anims.ITimeModifier
import net.liplum.math.Polar
import net.liplum.utils.TR
import net.liplum.utils.autoAnim
import net.liplum.utils.radian
import net.liplum.utils.sub

open class TMTRAINER(name: String) : ItemTurret(name) {
    @ClientOnly lateinit var CoreAnim: Animation
    @ClientOnly lateinit var EmptyCoreAnim: Animation
    @ClientOnly lateinit var HeadTR: TR
    @JvmField @ClientOnly var headMax = 0.45f
    @JvmField @ClientOnly var headMin = -3f
    @JvmField var CoreAnimFrames = 8
    @JvmField var maxVirusChargeSpeedUp = 2.5f

    init {
        drawer = object : DrawTurret() {
            lateinit var CoreAnim: Animation
            lateinit var EmptyCoreAnim: Animation
            lateinit var HeadTR: TR
            override fun load(b: Block) = b.run {
                super.load(this)
                CoreAnim = autoAnim("core", CoreAnimFrames, 60f)
                HeadTR = sub("head")
                EmptyCoreAnim = autoAnim("core-empty", CoreAnimFrames, 60f)
            }

            override fun icons(block: Block): Array<TextureRegion> =
                arrayOf(base, HeadTR, region)

            override fun drawTurret(t: Turret, b: TurretBuild) = (b as TMTRAINERBUILD).run {
                WhenNotPaused {
                    coreAnimObj.spend(Time.delta)
                    emptyCoreAnimObj.spend(Time.delta)
                    targetPol.a = rotation.radian
                    var tpr = targetPol.r
                    val delta = virusCharge * 0.001f
                    tpr = if (wasShooting) tpr - delta else tpr + delta
                    tpr = tpr.coerceIn(headMin, headMax)
                    targetPol.r = tpr
                }
                val drawRotation = rotation.draw
                val drawX = x + recoilOffset.x
                val drawY = y + recoilOffset.y
                Draw.rect(
                    HeadTR,
                    drawX + targetPol.x,
                    drawY + targetPol.y,
                    drawRotation
                )

                Drawf.shadow(region, drawX - elevation, drawY - elevation, drawRotation)
                super.draw(this)
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
    }

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

        override fun update() {
            super.update()
            val delta = if (wasShooting) delta() else -delta()
            virusCharge += delta / 2.5f
        }
    }
}