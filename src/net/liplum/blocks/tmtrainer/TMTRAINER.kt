package net.liplum.blocks.tmtrainer

import arc.graphics.g2d.Draw
import mindustry.content.Fx
import mindustry.world.blocks.defense.turrets.ItemTurret
import net.liplum.ClientOnly
import net.liplum.animations.anims.blocks.AutoAnimation
import net.liplum.utils.autoAnim

open class TMTRAINER(name: String) : ItemTurret(name) {
    lateinit var CoreAnim: AutoAnimation

    init {
        spread = 4f
        shots = 2
        alternate = true
        reloadTime = 5f
        restitution = 0.03f
        range = 110f
        shootCone = 15f
        ammoUseEffect = Fx.sporeSlowed
        health = 250
        inaccuracy = 1f
        rotateSpeed = 10f

        limitRange(20f)
    }

    override fun load() {
        super.load()
        CoreAnim = this.autoAnim("core", 5, 60f)
    }

    open inner class TMTRAINERBUILD : ItemTurretBuild() {
        override fun draw() {
            super.draw()
            ClientOnly {
                CoreAnim.draw {
                    Draw.rect(it, x, y, rotation - 90)
                }
            }
        }
    }
}