package net.liplum.render

import arc.graphics.Color
import arc.util.Align
import mindustry.Vars
import mindustry.gen.Building
import net.liplum.common.util.inViewField
import plumy.core.math.Point2f
import plumy.core.ClientOnly
import plumy.dsl.WorldXY

var ToastTimeFadePercent = 0.1f
var ToastTime = 180f
private val p1 = Point2f()
@JvmOverloads
fun String.postToastTextOn(
    b: Building,
    color: Color,
    useGlobalTime: Boolean = false,
    overwrite: Boolean = true,
    faded: Boolean = true,
) {
    ClientOnly {
        Toaster.post(b.id, ToastTime, useGlobalTime, overwrite) {
            if (!b.inViewField(b.block.clipSize)) return@post
            Text.drawText {
                setText(it, this@postToastTextOn)
                if (!b.isAdded) {
                    toast.duration *= 0.99f
                }
                it.color.set(color).a(
                    if (faded) fadeInOutPct(ToastTimeFadePercent)
                    else 1f
                )
                it.draw(
                    this@postToastTextOn, b.x,
                    b.y + b.block.size * Vars.tilesize / 2f,
                    Align.center
                )
            }
        }
    }
}

fun removeToastOn(b: Building) {
    ClientOnly {
        Toaster.remove(b.id)
    }
}
@JvmOverloads
fun String.postToastTextOn(
    id: Any,
    other: Building,
    color: Color,
    useGlobalTime: Boolean = false,
    overwrite: Boolean = true,
    faded: Boolean = true,
) {
    ClientOnly {
        Toaster.post(id, ToastTime, useGlobalTime, overwrite) {
            if (!p1.set(other.x, other.y).inViewField(other.block.clipSize)) return@post
            Text.drawText {
                setText(it, this@postToastTextOn)
                if (!other.isAdded) {
                    toast.duration *= 0.99f
                }
                it.color.set(color).a(
                    if (faded) fadeInOutPct(ToastTimeFadePercent)
                    else 1f
                )
                it.draw(
                    this@postToastTextOn, other.x,
                    other.y + other.block.size * Vars.tilesize / 2f,
                    Align.center
                )
            }
        }
    }
}
@JvmOverloads
fun String.postToastTextOnXY(
    id: Any,
    x: WorldXY, y: WorldXY,
    color: Color,
    clipSize: Float = 10f,
    useGlobalTime: Boolean = false,
    overwrite: Boolean = true,
    faded: Boolean = true,
) {
    ClientOnly {
        Toaster.post(id, ToastTime, useGlobalTime, overwrite) {
            if (!p1.set(x, y).inViewField(clipSize)) return@post
            Text.drawText {
                setText(it, this@postToastTextOnXY)
                it.color.set(color).a(
                    if (faded) fadeInOutPct(ToastTimeFadePercent)
                    else 1f
                )
                it.draw(
                    this@postToastTextOnXY, x,
                    y + clipSize * Vars.tilesize / 2f,
                    Align.center
                )
            }
        }
    }
}