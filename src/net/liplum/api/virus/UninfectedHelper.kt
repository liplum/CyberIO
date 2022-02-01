package net.liplum.api.virus

import mindustry.world.Block
import mindustry.world.blocks.environment.Floor

fun <T : Block> T.setUninfectedBlock(): T {
    UninfectedBlocks.registerBlock(this)
    return this
}

fun <T : Floor> T.setUninfectedFloor(): T {
    UninfectedBlocks.registerFloor(this)
    return this
}

fun <T : Floor> T.setUninfectedOverlay(): T {
    UninfectedBlocks.registerOverlay(this)
    return this
}