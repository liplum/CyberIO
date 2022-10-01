package net.liplum.render

import mindustry.gen.Building

interface ITreeSection<T> where T : Building {
    var children : MutableList<DrawSection<T>>
}

interface IMovableSection<T> where T : Building {
    var moves : MutableList<SectionMove<T>>
}

inline fun <T : Building> ITreeSection<T>.regionSection(
    suffix: String = "",
    config: RegionSection<T>.() -> Unit,
) {
    children += RegionSection<T>(suffix).apply(config)
}

inline fun <T : Building> IMovableSection<T>.addMove(
    config: SectionMove<T>.() -> Unit,
) {
    moves += SectionMove<T>().apply(config)
}