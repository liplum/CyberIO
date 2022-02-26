package net.liplum.utils

import arc.Core
import arc.graphics.g2d.TextureRegion
import mindustry.ctype.MappableContent

typealias TR = TextureRegion

/**
 * Gets the Texture Region of "sprites/{this}-{subName}"
 * @param subName the following name after a hyphen
 */
fun MappableContent.subA(subName: String): TR {
    return AtlasU.sub(this, subName)
}

fun TR.orSubA(obj: MappableContent, subName: String): TR {
    return if (Core.atlas.isFound(this))
        this
    else
        obj.subA(subName)
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
fun MappableContent.animA(subName: String?, number: Int): Array<TR> {
    return AtlasU.animation(this, subName, number)
}
/**
 * Gets an array of Texture Region of "sprites/{this}-{subName}" or "sprites/{this}" if subName is null.
 * @param subName the following name after a hyphen. If it's null, use the {this} name
 * @param isHorizontal if ture, splits it by row. Otherwise, splits it by column.
 * @param number the amount of frames in that image.
 */
fun MappableContent.animA(
    subName: String?,
    isHorizontal: Boolean,
    number: Int
): Array<TR> {
    return AtlasU.animation(this, subName, isHorizontal, number)
}