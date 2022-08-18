package net.liplum.blocks.tmtrainer

import arc.graphics.Color
import arc.math.Mathf
import net.liplum.R
import plumy.dsl.bundle
import plumy.core.arc.ColorRGB

object RandomName {
    const val InitColorCount = 255
    const val MaxColorNumber = 0xFFFFFF
    /**
     * Don't use '$', '@','§'
     */
    var AllChars = R.Bundle.RandomName.bundle.toCharArray().filter {
        it != '@' && it != '$'
    }.distinct().map { "$it" }
    val AllColors: Array<Color>
    var AllColorHex: Array<String>

    init {
        val allColorRgb888 = Array(InitColorCount) { Mathf.random(MaxColorNumber) }
        AllColors = Array(InitColorCount) { ColorRGB(allColorRgb888[it]) }
        AllColorHex = Array(InitColorCount) {
            "[#" + Integer.toHexString(allColorRgb888[it]) + ']'
        }
    }

    private val builder = StringBuilder()
    fun one(length: Int): String {
        builder.setLength(0)
        for (i in 0 until length) {
            builder.append(AllColorHex[Mathf.random(AllColorHex.size - 1)])
            builder.append(AllChars[Mathf.random(AllChars.size - 1)])
        }
        return builder.toString()
    }

    fun randomColorHex(): String {
        return AllColorHex[Mathf.random(AllColorHex.size - 1)]
    }

    fun randomColor(): Color {
        return AllColors[Mathf.random(AllColors.size - 1)]
    }

    fun randomTinted(text: String): String {
        return randomColorHex() + text + "[]"
    }

    fun randomChar() = AllChars[Mathf.random(AllChars.size - 1)]
    fun randomCharIndex() = Mathf.random(AllChars.size - 1)
    fun getChar(index: Int) = AllChars[index.coerceIn(0, AllChars.size - 1)]
    fun getColor(index: Int) = AllColors[index.coerceIn(0, AllChars.size - 1)]
}