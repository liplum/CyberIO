@file:JvmName("AtlasXH")

package net.liplum.utils

import mindustry.ctype.MappableContent
import net.liplum.lib.TR
import net.liplum.lib.TRs
import net.liplum.lib.utils.AtlasU
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
    return AtlasU.slice(tr, number, isHorizontal)
}
/**
 * Support content specific. see [String.sheet]
 */
fun String.sheetX(
    number: Int,
    isHorizontal: Boolean = true,
): TRs = AtlasU.slice(this.atlasX(), number, isHorizontal)