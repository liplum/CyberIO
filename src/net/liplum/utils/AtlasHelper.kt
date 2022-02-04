package net.liplum.utils

import arc.graphics.g2d.TextureRegion
import mindustry.ctype.MappableContent

typealias TR = TextureRegion

fun MappableContent.subA(subName: String): TR {
    return AtlasUtil.sub(this, subName)
}

fun MappableContent.animA(subName: String?, number: Int): Array<TR> {
    return AtlasUtil.animation(this, subName, number)
}

fun MappableContent.animA(
    subName: String?,
    isHorizontal: Boolean,
    number: Int
): Array<TR> {
    return AtlasUtil.animation(this, subName, isHorizontal, number)
}