package net.liplum.mdt.mixin

import mindustry.world.Block

fun Block.updatable() {
    update = true
    solid = true
}