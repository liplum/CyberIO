package net.liplum.render

import arc.Core
import arc.graphics.Blending
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.struct.Seq
import arc.util.Tmp
import mindustry.gen.Building
import mindustry.graphics.Drawf
import plumy.core.assets.EmptyTRs
import plumy.core.assets.TR
import plumy.core.assets.isFound
import plumy.core.math.clamp
import plumy.core.math.isZero
import plumy.core.math.radian

open class RegionSection<T>(
    var suffix: String = "",
) : DrawSection<T>(), ITreeSection<T> where T : Building {
    protected var childParam = SectionArgs<T>()
    var name: String? = null
    var regions = EmptyTRs
    var outlines = EmptyTRs
    /** If true, parts are mirrored across the turret. Requires -l and -r regions.  */
    var mirror = false
    /** If true, an outline is drawn under the part.  */
    var drawOutline = false
    /** If true, the base regions are drawn.*/
    var drawRegion = true
    var drawShadow = true
    var shadowElevation = 0.5f
    var shadowProgress: SectionProgress<T> = Sections.one
    /**
     *  Progress function for determining position/rotation.
     *  With clamp.
     */
    var progress: SectionProgress<T> = Sections.warmup
    var layer = -1f
    var layerOffset = 0f
    var outlineLayerOffset = -0.001f
    var x = 0f
    var y = 0f
    var rotation = 0f
    var moveX = 0f
    var moveY = 0f
    var moveRotation = 0f
    var blending: Blending = Blending.normal
    var color: Color? = null
    var colorTo: Color? = null
    var mixColor: Color? = null
    var mixColorTo: Color? = null
    override var children: MutableList<DrawSection<T>> = ArrayList()
    var moves = ArrayList<SectionMove<T>>()
    override fun draw(build: T, args: SectionArgs<T>) {
        val z = Draw.z()
        if (layer > 0) Draw.z(layer)
        Draw.z(Draw.z() + layerOffset)
        val prevZ = Draw.z()
        val prog: Float = progress(build).clamp
        var mx = moveX * prog
        var my = moveY * prog
        var mr = moveRotation * prog + rotation

        if (moves.size > 0) {
            for (i in 0 until moves.size) {
                val move = moves[i]
                val p = move.progress(build).clamp
                mx += move.x * p
                my += move.y * p
                mr += move.rotation * p
            }
        }
        val len = if (mirror) 2 else 1

        for (s in 0 until len) {
            val regionIndex = s.coerceAtMost(regions.size - 1)
            val region: TR? = if (drawRegion) regions[regionIndex] else null
            val sign = if (s == 0) 1f else -1f
            Tmp.v1.set(
                (x + mx) * sign * Draw.xscl,
                (y + my) * Draw.yscl
            ).rotateRadExact(args.rotation.radian)
            val rx = args.x + Tmp.v1.x
            val ry = args.y + Tmp.v1.y
            val rot = mr * sign + args.rotation
            Draw.xscl *= sign
            if (drawOutline && drawRegion) {
                Draw.z(prevZ + outlineLayerOffset)
                Draw.rect(outlines[regionIndex], rx, ry, rot)
                Draw.z(prevZ)
            }
            if (region != null && region.isFound) {
                if (drawShadow) {
                    Draw.z(prevZ - 0.1f)
                    val elevation = shadowElevation * build.shadowProgress().clamp
                    if (!elevation.isZero)
                        Drawf.shadow(region, rx - elevation, ry - elevation, rot)
                    Draw.z(prevZ)
                }
                run color@{
                    val color = color
                    val colorTo = colorTo
                    if (color != null && colorTo != null) {
                        Draw.color(color, colorTo, prog)
                    } else if (color != null) {
                        Draw.color(color)
                    }
                }
                run mixColor@{
                    val mixColor = mixColor
                    val mixColorTo = mixColorTo
                    if (mixColor != null && mixColorTo != null) {
                        Draw.mixcol(mixColor, mixColorTo, prog)
                    } else if (mixColor != null) {
                        Draw.mixcol(mixColor, mixColor.a)
                    }
                }
                Draw.blend(blending)
                Draw.rect(region, rx, ry, rot)
                Draw.blend()
                if (color != null) Draw.color()
            }
            Draw.xscl *= sign
        }

        Draw.color()
        Draw.mixcol()

        Draw.z(z)
        //draw child, if applicable - only at the end
        if (children.size > 0) {
            for (s in 0 until len) {
                val sign = if (s == 1) -1f else 1f
                Tmp.v1.set((x + mx) * sign, y + my).rotateRadExact(args.rotation.radian)
                childParam.apply {
                    x = args.x + Tmp.v1.x
                    y = args.y + Tmp.v1.y
                    rotation = s * sign + mr * sign + args.rotation
                }
                for (child in children) {
                    child.draw(build, childParam)
                }
            }
        }
    }

    override fun load(name: String) {
        val realName = this.name ?: (name + suffix)

        if (drawRegion) {
            if (mirror) {
                regions = arrayOf(
                    Core.atlas.find("$realName-r"),
                    Core.atlas.find("$realName-l")
                )
                outlines = arrayOf(
                    Core.atlas.find("$realName-r-outline"),
                    Core.atlas.find("$realName-l-outline")
                )
            } else {
                regions = arrayOf(Core.atlas.find(realName))
                outlines = arrayOf(Core.atlas.find("$realName-outline"))
            }
        }

        for (child in children) {
            child.load(name)
        }
    }

    override fun getOutlines(out: Seq<TextureRegion>) {
        if (drawOutline && drawRegion) {
            out.addAll(*regions)
        }
        for (child in children) {
            child.getOutlines(out)
        }
    }
}

inline fun <T : Building> DrawBuild<T>.regionSection(
    suffix: String = "",
    config: RegionSection<T>.() -> Unit,
) {
    sections += RegionSection<T>(suffix).apply(config)
}