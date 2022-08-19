package net.liplum.render

import arc.graphics.g2d.TextureRegion
import arc.struct.Seq
import mindustry.gen.Building
import plumy.core.math.Progress

abstract class DrawSection<T> where T : Building {
    abstract fun draw(build: T, args: SectionArgs<T>)
    abstract fun load(name: String)
    open fun getOutlines(out: Seq<TextureRegion>) {}
}
typealias SectionProgress<T> = T.() -> Progress
typealias BuildProgress = SectionProgress<Building>

object Sections {
    val warmup: BuildProgress = { warmup() }
    val progress: BuildProgress = { progress() }
}

data class SectionArgs<T>(
    var progress: SectionProgress<T> = Sections.warmup,
    var x: Float = 0f,
    var y: Float = 0f,
    var rotation: Float = 0f,
) where T : Building

data class SectionMove<T>(
    var progress: SectionProgress<T> = Sections.warmup,
    var x: Float = 0f,
    var y: Float = 0f,
    var rotation: Float = 0f,
) where T : Building
