package net.liplum.blocks.tmtrainer

import arc.Core
import arc.graphics.g2d.Draw
import mindustry.content.Fx
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.ItemTurret
import net.liplum.ClientOnly
import net.liplum.animations.anims.Animation
import net.liplum.animations.anims.IFrameIndexer
import net.liplum.animations.anims.blocks.ixSpeed
import net.liplum.draw
import net.liplum.math.PolarPos
import net.liplum.utils.TR
import net.liplum.utils.autoAnim
import net.liplum.utils.radian
import net.liplum.utils.subA

open class TMTRAINER(name: String) : ItemTurret(name) {
    @ClientOnly lateinit var CoreAnim: Animation
    @ClientOnly lateinit var HeadTR: TR
    @ClientOnly lateinit var EmptyCoreTR: TR
    @ClientOnly var headMax = 0.6f
    @ClientOnly var headMin = -3f
    var CoreAnimFrames = 8
    var maxVirusChargeTime = 5 * 60f
    var maxVirusChargeSpeedUp = 2.5f

    init {
        alternate = true
        shootEffect = Fx.sporeSlowed
        inaccuracy = 1f
        rotateSpeed = 10f
    }

    override fun load() {
        super.load()
        EmptyCoreTR = this.subA("core-empty")
        CoreAnim = this.autoAnim("core", CoreAnimFrames, 60f)
        HeadTR = this.subA("head")
    }

    override fun icons() = arrayOf(
        baseRegion, HeadTR, region
    )

    open inner class TMTRAINERBUILD : ItemTurretBuild() {
        @ClientOnly lateinit var coreIx: IFrameIndexer
        @ClientOnly var targetPol: PolarPos = PolarPos(headMax, 0f)
        open var virusCharge = 0f
            set(value) {
                field = value.coerceIn(0f, maxVirusChargeTime)
            }

        override fun update() {
            super.update()
            virusCharge = if (wasShooting) delta() else -delta()
        }

        override fun created() {
            ClientOnly {
                coreIx = CoreAnim.ixSpeed(this) {
                    (virusCharge / maxVirusChargeTime * maxVirusChargeTime)
                        .coerceAtLeast(1f)
                }
            }
        }

        override fun draw() {
            Draw.rect(baseRegion, x, y)
            Draw.color()

            Draw.z(Layer.turret)

            targetPol.a = rotation.radian
            var tr = targetPol.r
            val delta = reload * 0.1f
            tr = if (wasShooting) tr - delta else tr + delta
            tr = tr.coerceIn(headMin, headMax)
            targetPol.r = tr

            tr2.trns(rotation, -recoil)
            val drawRotation = rotation.draw
            Draw.rect(
                HeadTR,
                x + targetPol.toX() + tr2.x,
                y + targetPol.toY() + tr2.y,
                drawRotation
            )


            Drawf.shadow(region, x + tr2.x - elevation, y + tr2.y - elevation, drawRotation)
            drawer[this]
            CoreAnim.draw(coreIx) {
                Draw.rect(it, x, y, drawRotation)
            }

            if (Core.atlas.isFound(heatRegion)) {
                heatDrawer[this]
            }
        }
    }
}