package net.liplum.animations.anis

import mindustry.gen.Building
import mindustry.world.Block

interface IAniSMedBuild<TBlock : Block, TBuild : Building> {
    val aniStateM: AniStateM<TBlock, TBuild>
}