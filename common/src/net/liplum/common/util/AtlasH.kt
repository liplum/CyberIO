package net.liplum.common.util

import arc.Core
import net.liplum.lib.assets.TR
import net.liplum.lib.assets.TRs

fun TR.frames(number: Int, suffix: String = "-"): TRs =
    Array(number) { Core.atlas.find("$this$suffix$it") }

fun String.sheet(
    width: Int,
    height: Int = width,
): TRs = Core.atlas.find(this).sheet(width, height)

fun TR.sheet(
    width: Int,
    height: Int = width,
): TRs = this.split(width, height).flatten().toTypedArray()

fun String.sheetOneDirection(
    number: Int,
    byRow: Boolean = true,
): TRs = Core.atlas.find(this).sheetOneDirection(number, byRow)

fun TR.sheetOneDirection(
    number: Int,
    byRow: Boolean = true,
): TRs = run {
    val width = if (byRow) width / number else width
    val height = if (byRow) height else height / number
    if (byRow)
        this.split(width, height).run {
            Array(number) { i -> this[i][0] }
        }
    else
        this.split(width, height).run {
            Array(number) { i -> this[0][i] }
        }
}