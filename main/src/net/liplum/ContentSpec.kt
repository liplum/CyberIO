package net.liplum

import arc.graphics.Color
import net.liplum.ContentSpec.Erekir
import net.liplum.ContentSpec.Vanilla
import net.liplum.ContentSpecXInfo.Companion.needSuffixModVersion
import net.liplum.lib.TR
import net.liplum.lib.utils.bundle
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.atlas

enum class ContentSpec(
    val id: String
) {
    Vanilla("vanilla"),
    Erekir("erekir");

    override fun toString() = id

    companion object {
        @JvmStatic
        fun String.resolveContentSpec() = when (this) {
            "erekir" -> Erekir
            else -> Vanilla
        }
    }
}

class ContentSpecXInfo(
    val spec: ContentSpec
) {
    var needSuffixModVersion = false
    var needSuffixResource = false
    var color = Color()

    companion object {
        val ContentSpec.needSuffixModVersion: Boolean
            get() = ContentSpecExtraInfos[this]?.needSuffixModVersion ?: false
        val ContentSpec.needSuffixResource: Boolean
            get() = ContentSpecExtraInfos[this]?.needSuffixResource ?: false
        val ContentSpec.color: Color
            get() = ContentSpecExtraInfos[this]?.color ?: Color.white.cpy()
        val ContentSpecExtraInfos by lazy {
            mapOf(
                Erekir to ContentSpecXInfo(Erekir).apply {
                    needSuffixModVersion = true
                    needSuffixResource = true
                    color.set(R.C.HoloOrange)
                },
                Vanilla to ContentSpecXInfo(Vanilla).apply {
                    color.set(R.C.Holo)
                },
            )
        }
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
