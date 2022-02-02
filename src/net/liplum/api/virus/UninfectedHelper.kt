package net.liplum.api.virus

import mindustry.world.Block
import mindustry.world.blocks.environment.Floor

fun <T : Block> T.setUninfectedBlock(): T {
    UninfectedBlocksRegistry.block(this)
    return this
}

fun <T : Floor> T.setUninfectedFloor(): T {
    UninfectedBlocksRegistry.floor(this)
    return this
}

fun <T : Floor> T.setUninfectedOverlay(): T {
    UninfectedBlocksRegistry.overlay(this)
    return this
}

fun <T : Block> T.setUninfectedFloor(): T {
    UninfectedBlocksRegistry.floor(this)
    return this
}

fun <T : Block> T.setUninfectedOverlay(): T {
    UninfectedBlocksRegistry.overlay(this)
    return this
}