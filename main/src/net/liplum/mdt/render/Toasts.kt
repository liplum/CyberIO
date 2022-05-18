package net.liplum.mdt.render

import arc.graphics.Color
import arc.util.Align
import mindustry.gen.Building
import net.liplum.R
import net.liplum.lib.math.Point2f
import net.liplum.mdt.ClientOnly

var ToastTimeFadePercent = 0.1f
var ToastTime = 180f
private val p1 = Point2f()
@ClientOnly
fun String.postToastTextOn(
    other: Building,
    color: Color = R.C.RedAlert
) {
    Toaster.post(other.id, ToastTime) {
        if (!p1.set(other.x, other.y).inViewField(10f)) return@post
        Text.drawText {
            setText(it, this@postToastTextOn)
            if (!other.isAdded) {
                toast.duration *= 0.99f
            }
            it.color.set(color).a(fadeInOutPct(ToastTimeFadePercent))
            it.draw(this@postToastTextOn, other.x, other.y + 1f, Align.center)
        }
    }
}