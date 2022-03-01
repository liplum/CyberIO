package net.liplum.blocks.tmtrainer

import arc.graphics.g2d.Draw
import mindustry.content.Fx
import mindustry.world.blocks.defense.turrets.ItemTurret
import net.liplum.ClientOnly
import net.liplum.animations.anims.IFrameIndexer
import net.liplum.animations.anims.blocks.AutoAnimation
import net.liplum.animations.anims.blocks.ixByShooting
import net.liplum.utils.autoAnim

open class TMTRAINER(name: String) : ItemTurret(name) {
    lateinit var CoreAnim: AutoAnimation

    init {
        alternate = true
        shootEffect = Fx.sporeSlowed
        inaccuracy = 1f
        rotateSpeed = 10f

        limitRange(20f)
    }

    override fun load() {
        super.load()
        CoreAnim = this.autoAnim("core", 5, 60f)
    }

    open inner class TMTRAINERBUILD : ItemTurretBuild() {
        lateinit var coreIx: IFrameIndexer
        override fun created() {
            ClientOnly {
                coreIx = CoreAnim.ixByShooting(this)
            }
        }

        override fun draw() {
            super.draw()
            CoreAnim.draw(coreIx) {
                Draw.rect(it, x, y, rotation - 90)
            }
        }
    }
}