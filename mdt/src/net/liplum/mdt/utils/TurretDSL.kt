package net.liplum.mdt.utils

import mindustry.entities.bullet.BulletType
import mindustry.type.Item
import mindustry.world.blocks.defense.turrets.ItemTurret

fun ItemTurret.addAmmo(item: Item, bullet: BulletType) {
    ammoTypes.put(item, bullet)
}