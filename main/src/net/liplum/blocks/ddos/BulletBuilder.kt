package net.liplum.blocks.ddos

import mindustry.entities.bullet.BulletType
import mindustry.type.Item

interface IBulletBuilder<B : BulletType> {
    fun canGen(item: Item): Boolean
    fun gen(item: Item): B
}