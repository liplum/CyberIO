@file:JvmName("AbilityH")

package net.liplum.utils

import arc.Core
import mindustry.entities.abilities.Ability
import java.util.*

fun <T : Ability> Class<T>.localized(modID: String): String {
    var name: String = this.simpleName.replace("Ability", "")
    val sb = StringBuilder()
    for (i in name.indices) {
        val c = name[i]
        if (i != 0 && Character.isUpperCase(c)) {
            sb.append('-')
        }
        sb.append(c)
    }
    name = sb.toString().lowercase(Locale.getDefault())
    return Core.bundle["ability.$modID-$name"]
}