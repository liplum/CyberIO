@file:JvmName("AtlasH")

package net.liplum.mdt.utils

import arc.Core.atlas
import mindustry.Vars
import mindustry.ctype.MappableContent
import net.liplum.lib.TR
import net.liplum.lib.utils.AtlasU

/**
 * Gets the Texture Region of "sprites/{this}-{subName}"
 * @param subName the following name after a hyphen
 */
fun MappableContent.sub(subName: String): TR =
    atlas.find("${this.name}-$subName")

fun MappableContent.inMod(name: String): TR =
    atlas.find("${this.minfo.mod.name}-$name")

fun MappableContent.selfTR(): TR =
    atlas.find(name)
/**
 * In current loading mod
 */
val String.inMod: TR
    get() = atlas.find(Vars.content.transformName(this))

fun String.atlas(): TR =
    atlas.find(this)

fun TR.orSubA(obj: MappableContent, subName: String): TR =
    if (this.found())
        this
    else
        obj.sub(subName)

infix fun TR.or(texture: TR): TR =
    if (this.found())
        this
    else
        texture
/**
 * Gets an array of Texture Region from a list of images named in pattern "sprites/{this}-{subName}{number}" or "sprites/{this}{number}" if subName is null.
 * @param subName the following name after a hyphen. If it's null, use the {this}-{number} name
 * @param number the amount of frames in that image. Splits it by row.
 * @param start the start number of these images
 */
@JvmOverloads
fun MappableContent.subFrames(subName: String? = null, number: Int, start: Int = 0): Array<TR> =
    AtlasU.subFrames(if (subName != null) "$name-$subName" else name, start, number)
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
): Array<TR> {
    val identity = name + if (subName != null) "-$subName" else ""
    return AtlasU.sheet(identity, isHorizontal, number)
}

fun String.sheet(
    number: Int,
    isHorizontal: Boolean = true,
): Array<TR> = AtlasU.sheet(this, isHorizontal, number)
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
): Array<TR> =
    AtlasU.animation(this, subName, isHorizontal, number)
@JvmOverloads
fun TR.slice(
    count: Int,
    isHorizontal: Boolean = true,
): Array<TR> =
    AtlasU.slice(this, count, isHorizontal)

