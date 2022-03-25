package net.liplum

import arc.struct.ObjectMap
import arc.util.Log
import arc.util.Log.LogLevel
import net.liplum.lib.addLeft
import net.liplum.lib.addRight
import net.liplum.lib.buildCenterFillUntil
import net.liplum.lib.buildFill

object Clog {
    @JvmStatic
    @JvmOverloads
    fun <TK, TV> Map<TK, TV>.log(
        title: String,
        length: Int = 25,
        level: LogLevel = LogLevel.info,
        howToLog: (TK, TV) -> Unit
    ) {
        val infoHead = title.buildCenterFillUntil('=', length) addLeft "//" addRight "\\\\"
        Log.log(level, infoHead.toString())
        this.forEach(howToLog)
        val infoTail = buildFill('=', length) addLeft "\\\\" addRight "//"
        Log.log(level, infoTail.toString())
    }
    @JvmStatic
    @JvmOverloads
    fun <TK, TV> ObjectMap<TK, TV>.log(
        title: String,
        length: Int = 25,
        level: LogLevel = LogLevel.info,
        howToLog: (TK, TV) -> Unit
    ) {
        val infoHead = title.buildCenterFillUntil('=', length) addLeft "//" addRight "\\\\"
        Log.log(level, infoHead.toString())
        this.each(howToLog)
        val infoTail = buildFill('=', length) addLeft "\\\\" addRight "//"
        Log.log(level, infoTail.toString())
    }
}