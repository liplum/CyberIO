package net.liplum.mdt.render

import arc.graphics.Color
import arc.util.Align
import mindustry.gen.Building
import net.liplum.lib.math.Point2f
import net.liplum.mdt.ClientOnly

var ToastTimeFadePercent = 0.1f
var ToastTime = 180f
private val p1 = Point2f()
@JvmOverloads
fun String.postToastTextOn(
    other: Building,
    color: Color,
    useGlobalTime: Boolean = false,
    overwrite: Boolean = true,
    faded: Boolean = true,
) {
    ClientOnly {
        Toaster.post(other.id, ToastTime, useGlobalTime, overwrite) {
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
                it.draw(this@postToastTextOn, other.x, other.y + 1f, Align.center)
            }
        }
    }
}