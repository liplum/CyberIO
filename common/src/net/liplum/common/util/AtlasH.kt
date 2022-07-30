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
/**
 * Slice sprites in order of
 * ```
 * 1 2 3 4
 * 5 6 7 8
 * ```
 */
fun TR.sheet(
    tileWidth: Int,
    tileHeight: Int = tileWidth,
): TRs {
    val row = height / tileWidth
    val column = width / tileHeight
    return Array(row * column) { i ->
        val rowByColumn = split(tileWidth, tileHeight)
        rowByColumn[i % column][i / column]
    }
}

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