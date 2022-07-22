package net.liplum

import arc.graphics.Color
import net.liplum.common.utils.bundle
import net.liplum.lib.assets.TR
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.atlas

enum class ContentSpec(
    val id: String,
    var needSuffixModVersion: Boolean = false,
    var needSuffixResource: Boolean = false,
    var color: Color = Color(),
) {
    Vanilla(
        "vanilla",
        color = R.C.Holo.cpy()
    ),
    Erekir(
        "erekir",
        needSuffixModVersion = true,
        needSuffixResource = true,
        color = R.C.HoloOrange.cpy()
    );

    override fun toString() = id

    companion object {
        fun String.resolveContentSpec() = when (this.lowercase()) {
            "erekir" -> Erekir
            else -> Vanilla
        }

        fun String.tryResolveContentSpec() = when (this.lowercase()) {
            "vanilla" -> Vanilla
            "erekir" -> Erekir
            else -> null
        }
        @JvmField
        val candidateList = ContentSpec.values().joinToString(separator = ",", prefix = "[", postfix = "]")
    }
}

val String.spec: String
    get() = Var.ContentSpecific.suffixResource(this)

fun ContentSpec.suffixModVersion(version: String) =
    if (needSuffixModVersion) "$version-$id"
    else version

fun ContentSpec.suffixResource(version: String) =
    if (needSuffixModVersion) "$version-$id"
    else version
@ClientOnly
val ContentSpec.i18nName: String
    get() = "${Meta.ModID}.spec.$id.name".bundle
@ClientOnly
val ContentSpec.i18nDesc: String
    get() = "${Meta.ModID}.spec.$id.desc".bundle
@ClientOnly
val ContentSpec.icon: TR
    get() = R.Gen("spec-$id").atlas()
