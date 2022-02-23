package net.liplum.blocks.deleter

import mindustry.entities.bullet.BulletType
import mindustry.world.blocks.defense.turrets.LaserTurret

open class Deleter(name: String) : LaserTurret(name) {
    init {
        shootType = object :BulletType(){

        }
    }
}