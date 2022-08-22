package net.liplum.render

import arc.graphics.g2d.TextureRegion
import arc.struct.Seq
import mindustry.gen.Building
import plumy.core.math.Progress
import plumy.core.math.smooth
import plumy.core.math.smoother

abstract class DrawSection<T> where T : Building {
    abstract fun draw(build: T, args: SectionArgs<T>)
    open fun load(name: String){}
    open fun getOutlines(out: Seq<TextureRegion>) {}
}
typealias SectionProgress<T> = T.() -> Progress
typealias BuildProgress = SectionProgress<Building>

val <T> SectionProgress<T>.smooth: SectionProgress<T>
    get() = { this@smooth().smooth }

val <T> SectionProgress<T>.smoother: SectionProgress<T>
    get() = { this@smoother().smoother }

object Sections {
    val warmup: BuildProgress = { warmup() }
    val warmupSmooth: BuildProgress = { warmup().smooth }
    val progress: BuildProgress = { progress() }
    val progressSmooth: BuildProgress = { progress().smooth }
    val one: BuildProgress = { 1f }
    val zero: BuildProgress = { 0f }
}

data class SectionArgs<T>(
    var x: Float = 0f,
    var y: Float = 0f,
    var rotation: Float = 0f,
) where T : Building

data class SectionMove<T>(
    var progress: SectionProgress<T> = Sections.warmup,
    var rotationProgress: SectionProgress<T> = Sections.warmup,
    var x: Float = 0f,
    var y: Float = 0f,
    var rotation: Float = 0f,
) where T : Building
