@file:JvmName("AtlasH")

package net.liplum.mdt.utils

import arc.Core.atlas
import mindustry.Vars
import mindustry.ctype.MappableContent
import net.liplum.common.utils.sheetOneDirection
import net.liplum.lib.assets.TR
import net.liplum.lib.assets.TRs

fun String.atlas(): TR =
    atlas.find(this)
/**
 * Gets the Texture Region of "sprites/{this}-{subName}"
 * @param subName the following name after a hyphen
 */
fun MappableContent.sub(subName: String): TR =
    "${this.name}-$subName".atlas()

fun MappableContent.inMod(name: String): TR =
    "${this.minfo.mod.name}-$name".atlas()

fun MappableContent.atlas(): TR = name.atlas()
/**
 * In current loading mod
 */
val String.inMod: TR
    get() = Vars.content.transformName(this).atlas()

infix fun TR.or(texture: TR): TR =
    if (this.found())
        this
    else
        texture
/**
 * Gets an array of Texture Region from a single image named in pattern "sprites/{this}-{subName}" or "sprites/{this}" if subName is null.
 * @param subName the following name after a hyphen. If it's null, use the {this} name
 * @param number the amount of frames in that image. Splits it by row.
 */
@JvmOverloads
fun MappableContent.sheet(
    subName: String? = null,
    number: Int,
    isHorizontal: Boolean = true,
): TRs {
    val identity = name + if (subName != null) "-$subName" else ""
    return identity.sheetOneDirection(number, isHorizontal)
}
/**
 * Gets an array of Texture Region of "sprites/{this}-{subName}" or "sprites/{this}" if subName is null.
 * @param subName the following name after a hyphen. If it's null, use the {this} name
 * @param isHorizontal if ture, splits it by row. Otherwise, splits it by column.
 * @param number the amount of frames in that image.
 */
@JvmOverloads
fun MappableContent.anim(
    subName: String? = null,
    isHorizontal: Boolean = true,
    number: Int,
): TRs {
    val identity = name + if (subName != null) "-$subName-anim" else "-anim"
    return identity.sheetOneDirection(number, isHorizontal)
}
