package net.liplum.holo

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.geom.Vec2
import arc.util.Tmp
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Layer
import net.liplum.S
import net.liplum.render.DrawSection
import net.liplum.render.SectionArgs
import net.liplum.render.SectionProgress
import net.liplum.render.Sections
import plumy.dsl.DrawLayer

class HoloProjectingSection<T> : DrawSection<T>() where T : Building {
    val v = Vec2()
    var center: T.() -> Vec2 = { v.set(x, y) }
    var alphaProgress :SectionProgress<T> = Sections.warmupSmooth
    var color = S.Hologram
    var x = 0f
    var y = 0f
    var width = 6f
    var moveRotation = 0f
    var isTopRightOrBottomLeft = true
    override fun draw(build: T, args: SectionArgs<T>) {
        val o = build.center()
        Tmp.v1.set(
            x * Draw.xscl,
            y * Draw.yscl
        )
        val rx = args.x + Tmp.v1.x
        val ry = args.y + Tmp.v1.y
        DrawLayer(Layer.buildBeam) {
            Draw.color(color)
            Draw.alpha(build.alphaProgress())
            if (Vars.renderer.animateShields) {
                val x1: Float
                val y1: Float
                val x2: Float
                val y2: Float
                if (isTopRightOrBottomLeft) {
                    x1 = rx + width
                    y1 = ry - width
                    x2 = rx - width
                    y2 = ry + width
                } else {
                    x1 = rx + width
                    y1 = ry + width
                    x2 = rx - width
                    y2 = ry - width
                }
                Fill.tri(x1, y1, x2, y2, o.x, o.y)
            }
        }
    }
}