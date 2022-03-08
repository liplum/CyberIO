package net.liplum.utils

import arc.Core
import arc.graphics.g2d.TextureRegion
import mindustry.ctype.MappableContent

typealias TR = TextureRegion

/**
 * Gets the Texture Region of "sprites/{this}-{subName}"
 * @param subName the following name after a hyphen
 */
fun MappableContent.sub(subName: String): TR {
    return AtlasU.sub(this, subName)
}

fun TR.orSubA(obj: MappableContent, subName: String): TR {
    return if (Core.atlas.isFound(this))
        this
    else
        obj.sub(subName)
}

infix fun TR.or(texture: TR): TR {
    return if (Core.atlas.isFound(this))
        this
    else
        texture
}
/**
 * Gets an array of Texture Region of "sprites/{this}-{subName}" or "sprites/{this}" if subName is null.
 * @param subName the following name after a hyphen. If it's null, use the {this} name
 * @param number the amount of frames in that image. Splits it by row.
 */
fun MappableContent.anim(subName: String? = null, number: Int): Array<TR> {
    return AtlasU.animation(this, subName, number)
}
/**
 * Gets an array of Texture Region from a list of images named in pattern "sprites/{this}-{subName}-{number}" or "sprites/{this}-{number}" if subName is null.
 * @param subName the following name after a hyphen. If it's null, use the {this}-{number} name
 * @param number the amount of frames in that image. Splits it by row.
 * @param start the start number of those images
 */
fun MappableContent.subFrames(subName: String? = null, number: Int, start: Int = 0): Array<TR> {
    return AtlasU.subFrames(this, subName, start, number)
}
/**
 * Gets an array of Texture Region from a single image named in pattern "sprites/{this}-{subName}" or "sprites/{this}" if subName is null.
 * @param subName the following name after a hyphen. If it's null, use the {this} name
 * @param number the amount of frames in that image. Splits it by row.
 */
fun MappableContent.sheet(subName: String? = null, number: Int): Array<TR> {
    return AtlasU.sheet(this, subName, number)
}
/**
 * Gets an array of Texture Region of "sprites/{this}-{subName}" or "sprites/{this}" if subName is null.
 * @param subName the following name after a hyphen. If it's null, use the {this} name
 * @param isHorizontal if ture, splits it by row. Otherwise, splits it by column.
 * @param number the amount of frames in that image.
 */
fun MappableContent.anim(
    subName: String? = null,
    isHorizontal: Boolean,
    number: Int
): Array<TR> {
    return AtlasU.animation(this, subName, isHorizontal, number)
}