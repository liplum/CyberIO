package net.liplum.blocks.tmtrainer

import arc.math.Mathf
import net.liplum.R
import net.liplum.common.util.bundle

object RandomName {
    const val InitColorCount = 255
    const val MaxColorNumber = 0xFFFFFF
    /**
     * Don't use '$', '@','§'
     */
    var AllChars = R.Bundle.RandomName.bundle.toCharArray()
    var AllColors = Array(InitColorCount) {
        "[#" + Integer.toHexString(Mathf.random(MaxColorNumber)) + ']'
    }
    private val builder = StringBuilder()
    fun one(length: Int): String {
        builder.setLength(0)
        for (i in 0 until length) {
            builder.append(AllColors[Mathf.random(AllColors.size - 1)])
            builder.append(AllChars[Mathf.random(AllChars.size - 1)])
        }
        return builder.toString()
    }

    fun oneColor(): String {
        return AllColors[Mathf.random(AllColors.size - 1)]
    }

    fun randomTinted(text: String): String {
        return oneColor() + text + "[]"
    }
}