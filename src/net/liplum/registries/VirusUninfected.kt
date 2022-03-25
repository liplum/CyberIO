package net.liplum.registries

import mindustry.content.Blocks.*
import mindustry.world.Block
import net.liplum.api.virus.setUninfected
import net.liplum.api.virus.setUninfectedFloor

object VirusUninfected {
    @JvmStatic
    fun load() {
        floor(
            air,
            space,
            water,
            deepwater,
        )
        block(
            itemSource,
            liquidSource,
            powerSource,
            itemVoid,
            liquidVoid,
            powerVoid,
        )
    }

    fun floor(vararg blocks: Block) {
        for (block in blocks) {
            block.setUninfectedFloor()
        }
    }

    fun block(vararg blocks: Block) {
        for (block in blocks) {
            block.setUninfected()
        }
    }
}