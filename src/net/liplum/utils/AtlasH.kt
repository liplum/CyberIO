package net.liplum.utils

import arc.Core
import arc.graphics.g2d.TextureRegion
import mindustry.Vars
import mindustry.ctype.MappableContent
import net.liplum.Meta

typealias TR = TextureRegion

/**
 * Gets the Texture Region of "sprites/{this}-{subName}"
 * @param subName the following name after a hyphen
 */
fun MappableContent.sub(subName: String): TR =
    Core.atlas.find("${this.name}-$subName")

fun MappableContent.inMod(name: String): TR =
    Core.atlas.find("${this.minfo.mod.name}-$name")

fun String.inMod(): TR =
    Core.atlas.find(Vars.content.transformName(this))

fun String.inCio(): TR =
    Core.atlas.find("${Meta.ModID}-$this")

fun TR.orSubA(obj: MappableContent, subName: String): TR =
    if (Core.atlas.isFound(this))
        this
    else
        obj.sub(subName)

infix fun TR.or(texture: TR): TR =
    if (Core.atlas.isFound(this))
        this
    else
        texture
/**
 * Gets an array of Texture Region from a list of images named in pattern "sprites/{this}-{subName}-{number}" or "sprites/{this}-{number}" if subName is null.
 * @param subName the following name after a hyphen. If it's null, use the {this}-{number} name
 * @param number the amount of frames in that image. Splits it by row.
 * @param start the start number of those images
 */
@JvmOverloads
fun MappableContent.subFrames(subName: String? = null, number: Int, start: Int = 0): Array<TR> =
    AtlasU.subFrames(this, subName, start, number)
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
): Array<TR> =
    AtlasU.sheet(this, subName, isHorizontal, number)
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
    number: Int
): Array<TR> =
    AtlasU.animation(this, subName, isHorizontal, number)
@JvmOverloads
fun TR.slice(
    count: Int,
    isHorizontal: Boolean = true,
): Array<TR> =
    AtlasU.slice(this, count, isHorizontal)

