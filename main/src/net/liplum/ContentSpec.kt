package net.liplum

import net.liplum.lib.TR
import net.liplum.lib.utils.bundle
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.atlas

enum class ContentSpec(
    val id: String,
    val needSuffixModVersion: Boolean,
    val needSuffixResource: Boolean,
) {
    Vanilla("vanilla", false, false),
    Erekir("erekir", true, true);

    override fun toString() = id

    companion object {
        @JvmStatic
        fun String.resolveContentSpec() = when (this) {
            "erekir" -> Erekir
            else -> Vanilla
        }
    }
}

val String.spec: String
    get() = CioMod.ContentSpecific.suffixResource(this)

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
