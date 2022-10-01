package net.liplum

import arc.struct.ObjectMap
import arc.util.Log
import arc.util.Log.LogLevel
import net.liplum.common.addLeft
import net.liplum.common.addRight
import net.liplum.common.BuildCenterFillUntil
import net.liplum.common.fill
import java.io.PrintWriter
import java.io.StringWriter

object CLog {
    @JvmStatic
    fun err(text: String, vararg args: Any?) {
        Log.log(LogLevel.err, "[${Meta.NameNoSpace}]$text", *args)
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
        Log.log(LogLevel.info, "[${Meta.NameNoSpace}]$text", *args)
    }
    @JvmStatic
    fun info(`object`: Any) {
        info("$`object`")
    }
    @JvmStatic
    fun warn(text: String, vararg args: Any?) {
        Log.log(LogLevel.warn, "[${Meta.NameNoSpace}]$text", *args)
    }
    @JvmStatic
    @JvmOverloads
    inline fun <TK, TV> Map<TK, TV>.log(
        title: String,
        length: Int = 25,
        level: LogLevel = LogLevel.info,
        howToLog: (TK, TV) -> Unit,
    ) {
        val infoHead = title.BuildCenterFillUntil('=', length) addLeft "//" addRight "\\\\"
        Log.log(level, infoHead.toString())
        this.forEach {
            howToLog(it.key, it.value)
        }
        val infoTail = fill(length, '=') addLeft "\\\\" addRight "//"
        Log.log(level, infoTail.toString())
    }
    @JvmStatic
    @JvmOverloads
    inline fun <TK, TV> ObjectMap<TK, TV>.log(
        title: String,
        length: Int = 25,
        level: LogLevel = LogLevel.info,
        howToLog: (TK, TV) -> Unit,
    ) {
        val infoHead = title.BuildCenterFillUntil('=', length) addLeft "//" addRight "\\\\"
        Log.log(level, infoHead.toString())
        for (entry in entries()) {
            howToLog(entry.key, entry.value)
        }
        val infoTail = fill(length, '=') addLeft "\\\\" addRight "//"
        Log.log(level, infoTail.toString())
    }
}