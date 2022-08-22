package net.liplum.holo

import arc.Core
import arc.graphics.g2d.Draw
import arc.util.Tmp
import mindustry.gen.Building
import mindustry.graphics.Drawf
import net.liplum.render.*
import plumy.core.assets.EmptyTR
import plumy.core.assets.isFound
import plumy.core.math.clamp
import plumy.core.math.radian

class ProjectorSection<T>(
    var suffix: String = "",
) : DrawSection<T>(), ITreeSection<T> where T : Building {
    var childParam = SectionArgs<T>()
    var region = EmptyTR
    var progress: SectionProgress<T> = Sections.warmup
    var x = 0f
    var y = 0f
    var layer = -1f
    var layerOffset = 0f
    var rotation: T.() -> Float = { 0f }
    var drawShadow = true
    var shadowElevation = 0.5f
    var moveX = 0f
    var moveY = 0f
    override var children: MutableList<DrawSection<T>> = ArrayList()
    override fun draw(build: T, args: SectionArgs<T>) {
        val z = Draw.z()
        if (layer > 0) Draw.z(layer)
        Draw.z(Draw.z() + layerOffset)
        val prevZ = Draw.z()
        val prog: Float = progress(build).clamp
        val mx = moveX * prog
        val my = moveY * prog
        val mr = rotation(build)
        Tmp.v1.set(
            (x + mx) * Draw.xscl,
            (y + my) * Draw.yscl
        ).rotateRadExact((mr+args.rotation).radian)
        val rx = args.x + Tmp.v1.x
        val ry = args.y + Tmp.v1.y
        val rot = mr + args.rotation
        if (region.isFound) {
            if (drawShadow) {
                Draw.z(prevZ - 0.1f)
                Drawf.shadow(region, rx - shadowElevation, ry - shadowElevation, rot)
                Draw.z(prevZ)
            }
            Draw.rect(region, rx, ry, rot)
        }

        Draw.z(z)
        //draw child, if applicable - only at the end
        if (children.size > 0) {
            Tmp.v1.set((x + mx), y + my).rotateRadExact(args.rotation.radian)
            childParam.apply {
                x = args.x + Tmp.v1.x
                y = args.y + Tmp.v1.y
                rotation = mr + args.rotation
            }
            for (child in children) {
                child.draw(build, childParam)
            }
        }
    }

    override fun load(name: String) {
        val realName = name + suffix

        region = Core.atlas.find(realName)

        for (child in children) {
            child.load(name)
        }
    }
}

inline fun <T : Building> DrawBuild<T>.projectorSection(
    suffix: String = "",
    config: ProjectorSection<T>.() -> Unit,
) {
    sections += ProjectorSection<T>(suffix).apply(config)
}
