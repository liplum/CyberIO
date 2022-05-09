package net.liplum.api.virus

import mindustry.world.Block
import mindustry.world.blocks.environment.Floor
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup

fun <T : Block> T.setUninfected(): T {
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

fun <T : BlockFlag> T.setUninfected(): T {
    UninfectedBlocksRegistry.flag(this)
    return this
}

fun <T : BlockGroup> T.setUninfected(): T {
    UninfectedBlocksRegistry.group(this)
    return this
}