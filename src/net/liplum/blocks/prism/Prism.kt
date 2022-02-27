package net.liplum.blocks.prism

import mindustry.content.Items
import mindustry.entities.bullet.BulletType
import mindustry.entities.bullet.LaserBulletType
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.gen.Groups
import mindustry.gen.Unit
import mindustry.type.Category
import mindustry.type.Item
import mindustry.world.Block
import mindustry.world.blocks.defense.turrets.PowerTurret
import mindustry.world.blocks.defense.turrets.Turret

open class Prism(name: String) : PowerTurret(name) {

    //open class Prism(name: String) : Block(name) {
    //copy your lasers
    //
    var realrange = 30f

    init {
        solid = true
        update = true
        absorbLasers = true

    }
    open inner class PrismLaser: LaserBulletType(){
        init {
        }

    }

    open inner class PrismBuild : PowerTurretBuild() {
        override fun updateTile() {
            Groups.bullet.intersect(
                x - 10f,
                y - 10f,
                realrange,
                realrange
            ) {
                if (it.type is LaserBulletType) {

                    it.absorb()
                }
            }

        }

    }
}