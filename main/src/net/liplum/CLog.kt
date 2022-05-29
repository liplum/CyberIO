package net.liplum

import arc.struct.ObjectMap
import arc.util.Log
import arc.util.Log.LogLevel
import net.liplum.lib.addLeft
import net.liplum.lib.addRight
import net.liplum.lib.buildCenterFillUntil
import net.liplum.lib.buildFill
import java.io.PrintWriter
import java.io.StringWriter

object CLog {
    @JvmStatic
    fun err(text: String, vararg args: Any?) {
        Log.log(LogLevel.err, "[${Meta.NameX}]$text", *args)
    }
    @JvmStatic
    fun err(th: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        th.printStackTrace(pw)
        err(sw.toString())
    }
    @JvmStatic
    fun err(text: String, th: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        th.printStackTrace(pw)
        err("$text: $sw")
    }
    @JvmStatic
    fun info(text: String, vararg args: Any?) {
        Log.log(LogLevel.info, "[${Meta.NameX}]$text", *args)
    }
    @JvmStatic
    fun info(`object`: Any) {
        info("$`object`")
    }
    @JvmStatic
    fun warn(text: String, vararg args: Any?) {
        Log.log(LogLevel.warn, "[${Meta.NameX}]$text", *args)
    }
    @JvmStatic
    @JvmOverloads
    inline fun <TK, TV> Map<TK, TV>.log(
        title: String,
        length: Int = 25,
        level: LogLevel = LogLevel.info,
        howToLog: (TK, TV) -> Unit
    ) {
        val infoHead = title.buildCenterFillUntil('=', length) addLeft "//" addRight "\\\\"
        Log.log(level, infoHead.toString())
        this.forEach {
            howToLog(it.key, it.value)
        }
        val infoTail = buildFill('=', length) addLeft "\\\\" addRight "//"
        Log.log(level, infoTail.toString())
    }
    @JvmStatic
    @JvmOverloads
    inline fun <TK, TV> ObjectMap<TK, TV>.log(
        title: String,
        length: Int = 25,
        level: LogLevel = LogLevel.info,
        howToLog: (TK, TV) -> Unit
    ) {
        val infoHead = title.buildCenterFillUntil('=', length) addLeft "//" addRight "\\\\"
        Log.log(level, infoHead.toString())
        for (entry in entries()) {
            howToLog(entry.key, entry.value)
        }
        val infoTail = buildFill('=', length) addLeft "\\\\" addRight "//"
        Log.log(level, infoTail.toString())
    }
}