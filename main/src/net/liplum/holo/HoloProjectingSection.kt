package net.liplum.holo

import arc.graphics.g2d.Draw
import arc.math.geom.Vec2
import arc.util.Tmp
import mindustry.gen.Building
import mindustry.graphics.Layer
import net.liplum.Var
import net.liplum.render.G
import net.liplum.render.*
import plumy.dsl.DrawLayer

class HoloProjectingSection<T> : DrawSection<T>() where T : Building {
    val v = Vec2()
    var center: T.() -> Vec2 = { v.set(x, y) }
    var alphaProgress: SectionProgress<T> = Sections.warmupSmooth
    var color = Var.Hologram
    var x = 0f
    var y = 0f
    var width = 6f
    var moveRotation = 0f
    var isTopRightOrBottomLeft = true
    // TODO: rotation
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
            if (isTopRightOrBottomLeft) {
                G.triangleShield(
                    x1 = rx + width, y1 = ry - width,
                    x2 = rx - width, y2 = ry + width,
                    o.x, o.y
                )
            } else {
                G.triangleShield(
                    x1 = rx + width, y1 = ry + width,
                    x2 = rx - width, y2 = ry - width,
                    o.x, o.y
                )
            }
        }
    }
}
inline fun <T : Building> ITreeSection<T>.holoProjectingSection(
    config: HoloProjectingSection<T>.() -> Unit,
) {
    children += HoloProjectingSection<T>().apply(config)
}