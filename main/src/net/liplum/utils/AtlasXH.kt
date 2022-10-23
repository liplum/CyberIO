@file:JvmName("AtlasXH")

package net.liplum.utils

import mindustry.ctype.MappableContent
import net.liplum.common.util.StartWithHyphen
import net.liplum.common.util.sheet
import net.liplum.common.util.sheetOneDirection
import net.liplum.spec
import plumy.core.assets.TR
import plumy.core.assets.TRs
import plumy.dsl.sprite

/**
 * Support content specific. see [String.sprite]
 */
val String.spriteX: TR
    get() = this.spec.sprite or this.sprite
/**
 * Support content specific. see [MappableContent.sub]
 */
@StartWithHyphen
fun MappableContent.subX(suffix: String): TR =
    "${this.name}-$suffix".spriteX

val MappableContent.spriteX: TR
    get() = name.spriteX

fun MappableContent.inModX(name: String): TR =
    "${this.minfo.mod.name}-$name".spriteX
/**
 * Support content specific. see [MappableContent.sheet]
 */
@JvmOverloads
fun MappableContent.sheetX(
    subName: String? = null,
    number: Int,
    isHorizontal: Boolean = true,
): TRs {
    val id = name + if (subName != null) "-$subName" else ""
    val tr = id.spriteX
    return tr.sheetOneDirection(number, isHorizontal)
}
/**
 * Support content specific. see [String.sheet]
 */
fun String.sheetX(
    number: Int,
    isHorizontal: Boolean = true,
): TRs = this.spriteX.sheetOneDirection(number, isHorizontal)