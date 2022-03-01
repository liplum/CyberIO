package net.liplum.blocks.tmtrainer

import arc.Core
import arc.graphics.g2d.Draw
import mindustry.content.Fx
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.defense.turrets.ItemTurret
import net.liplum.ClientOnly
import net.liplum.animations.anims.IFrameIndexer
import net.liplum.animations.anims.blocks.AutoAnimation
import net.liplum.animations.anims.blocks.ixByShooting
import net.liplum.math.PolarPos
import net.liplum.utils.TR
import net.liplum.utils.autoAnim
import net.liplum.utils.radian
import net.liplum.utils.subA

open class TMTRAINER(name: String) : ItemTurret(name) {
    @ClientOnly lateinit var CoreAnim: AutoAnimation
    @ClientOnly lateinit var HeadTR: TR
    @ClientOnly var headMax = 0.6f
    @ClientOnly var headMin = -3f

    init {
        alternate = true
        shootEffect = Fx.sporeSlowed
        inaccuracy = 1f
        rotateSpeed = 10f
    }

    override fun load() {
        super.load()
        CoreAnim = this.autoAnim("core", 5, 60f)
        HeadTR = this.subA("head")
    }

    open inner class TMTRAINERBUILD : ItemTurretBuild() {
        @ClientOnly lateinit var coreIx: IFrameIndexer
        @ClientOnly var targetPol: PolarPos = PolarPos(headMax, 0f)
        override fun created() {
            ClientOnly {
                coreIx = CoreAnim.ixByShooting(this)
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

            Draw.rect(
                HeadTR,
                x + targetPol.toX() + tr2.x,
                y + targetPol.toY() + tr2.y,
                rotation - 90
            )


            Drawf.shadow(region, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90)
            drawer[this]

            CoreAnim.draw(coreIx) {
                Draw.rect(it, x, y, rotation - 90)
            }

            if (Core.atlas.isFound(heatRegion)) {
                heatDrawer[this]
            }
        }
    }
}