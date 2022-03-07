package net.liplum.animations.anis

import mindustry.gen.Building
import mindustry.world.Block

fun <TBlock, TBuild> IAniSMed<TBlock, TBuild>.config(config: AniConfig<TBlock, TBuild>.() -> Unit)
        : AniConfig<TBlock, TBuild>
        where TBlock : Block, TBuild : Building {
    this.createAniConfig().config()
    return aniConfig
}
