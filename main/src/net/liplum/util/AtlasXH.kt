@file:JvmName("AtlasXH")

package net.liplum.util

import mindustry.ctype.MappableContent
import net.liplum.common.util.sheet
import net.liplum.common.util.sheetOneDirection
import net.liplum.lib.assets.TR
import net.liplum.lib.assets.TRs
import net.liplum.mdt.utils.atlas
import net.liplum.mdt.utils.or
import net.liplum.mdt.utils.sheet
import net.liplum.mdt.utils.sub
import net.liplum.spec

/**
 * Support content specific. see [String.atlas]
 */
fun String.atlasX(): TR {
    val spec = this.spec
    return spec.atlas() or this.atlas()
}
/**
 * Support content specific. see [MappableContent.sub]
 */
fun MappableContent.subX(subName: String): TR =
    "${this.name}-$subName".atlasX()

fun MappableContent.atlasX(): TR =
    name.atlasX()

fun MappableContent.inModX(name: String): TR =
    "${this.minfo.mod.name}-$name".atlasX()
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
    val tr = id.atlasX()
    return tr.sheetOneDirection(number, isHorizontal)
}
/**
 * Support content specific. see [String.sheet]
 */
fun String.sheetX(
    number: Int,
    isHorizontal: Boolean = true,
): TRs = this.atlasX().sheetOneDirection(number, isHorizontal)