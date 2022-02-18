package net.liplum.blocks.prism

import mindustry.entities.bullet.LaserBulletType
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.gen.Groups
import mindustry.gen.Unit
import mindustry.world.Block

open class Prism(name: String) : Block(name) {
    //copy your lasers
    //
    var range = 30f
    fun copylaser() {}
    fun callAttack(ownunit: Unit) {}
    fun rebound(laserbullet: LaserBulletType) {}

    init {
        solid = true
        update = true
        absorbLasers = true
    }

    open inner class PrismBuild : Building() {
        override fun updateTile() {
            Groups.bullet.intersect(
                x - 10f,
                y - 10f,
                range,
                range
            ) {
                it.absorb()
            }

            /*Groups.bullet.each({
                it.type.absorbable
            }) {
                it.absorb()
            }*/
        }
    }
}