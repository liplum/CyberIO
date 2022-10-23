@file:JvmName("AtlasH")

package net.liplum.utils

import mindustry.Vars
import mindustry.ctype.MappableContent
import net.liplum.common.util.StartWithHyphen
import net.liplum.common.util.sheetOneDirection
import plumy.core.assets.TR
import plumy.core.assets.TRs
import plumy.dsl.sprite

/**
 * Gets the Texture Region of "sprites/{this}-{subName}"
 * @param subName the following name after a hyphen
 */
@StartWithHyphen
fun MappableContent.sub(subName: String): TR =
    "${this.name}-$subName".sprite
@StartWithHyphen
fun MappableContent.inMod(name: String): TR =
    "${this.minfo.mod.name}-$name".sprite

fun MappableContent.atlas(): TR = name.sprite
/**
 * In current loading mod
 */
val String.inMod: TR
    get() = Vars.content.transformName(this).sprite

infix fun TR.or(texture: TR): TR =
    if (this.found()) this
    else texture
/**
 * Gets an array of Texture Region from a single image named in pattern "sprites/{this}-{subName}" or "sprites/{this}" if subName is null.
 * @param subName the following name after a hyphen. If it's null, use the {this} name
 * @param number the amount of frames in that image. Splits it by row.
 */
@StartWithHyphen
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
@StartWithHyphen
fun MappableContent.anim(
    subName: String? = null,
    isHorizontal: Boolean = true,
    number: Int,
): TRs {
    val identity = name + if (subName != null) "-$subName-anim" else "-anim"
    return identity.sheetOneDirection(number, isHorizontal)
}
