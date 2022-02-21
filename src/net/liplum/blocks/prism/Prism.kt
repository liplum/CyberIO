package net.liplum.blocks.prism

import mindustry.content.Items
import mindustry.entities.bullet.LaserBulletType
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.gen.Groups
import mindustry.gen.Unit
import mindustry.type.Category
import mindustry.type.Item
import mindustry.world.Block
import mindustry.world.blocks.defense.turrets.Turret

open class Prism(name: String) : Turret(name) {

    //open class Prism(name: String) : Block(name) {
    //copy your lasers
    //
    var range = 30f
    fun copylaser() {}
    fun callAttack(ownunit: Unit) {}
    fun rebound(laserbullet: LaserBulletType) {}
    fun fire(ownunit: Unit){}

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
    open fun alertstate(){
        if(true){

        }
    }
}