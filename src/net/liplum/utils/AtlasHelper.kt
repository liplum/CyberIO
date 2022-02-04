package net.liplum.utils

import arc.graphics.g2d.TextureRegion
import mindustry.ctype.MappableContent

fun MappableContent.subA(subName: String): TextureRegion {
    return AtlasUtil.sub(this, subName)
}

fun MappableContent.animA(subName: String?, number: Int): Array<TextureRegion> {
    return AtlasUtil.animation(this, subName, number)
}

fun MappableContent.animA(
    subName: String?,
    isHorizontal: Boolean,
    number: Int
): Array<TextureRegion> {
    return AtlasUtil.animation(this, subName, isHorizontal, number)
}