package net.liplum.common

object TimeH {
    const val SecPreDay = 24 * 60 * 60
    const val SecPreHour = 60 * 60
    const val SecPreMin = 60
    const val MilliPreSec = 1000
    @JvmStatic
    fun curTimeSec(): Long {
        return System.currentTimeMillis() / MilliPreSec
    }
    @JvmStatic
    @JvmOverloads
    fun toSec(
        day: Int = 0,
        hour: Int = 0,
        min: Int = 0,
        sec: Int = 0,
    ): Int = day * SecPreDay +
            hour * SecPreHour +
            min * SecPreMin +
            sec
    @JvmStatic
    fun toTimeFullString(
        sec: Int,
        separator: Char = ':',
    ): String {
        val days = sec / SecPreDay
        val hours = (sec % SecPreDay) / SecPreHour
        val mins = ((sec % SecPreDay) % SecPreHour) / SecPreMin
        val secs = ((sec % SecPreDay) % SecPreHour) % SecPreMin
        return toTimeString(days, hours, mins, secs, separator)
    }
    @JvmStatic
    @JvmOverloads
    fun toTimeString(
        day: Int = 0,
        hour: Int = 0,
        min: Int = 0,
        sec: Int = 0,
        separator: Char = ':',
    ): String {
        val sb = StringBuilder()
        val dayAdded = sb.tryAppendTimeFragment(day, separator, false)
        val hourAdded = sb.tryAppendTimeFragment(hour, separator, dayAdded)
        val minAdded = sb.tryAppendTimeFragment(min, separator, hourAdded)
        if (minAdded && sec < 10)
            sb.append('0')
        sb.append(sec)

        return sb.toString()
    }
    @JvmStatic
    private fun StringBuilder.tryAppendTimeFragment(
        value: Int, separator: Char, needZeroFill: Boolean = true,
    ): Boolean {
        if (value > 0) {
            if (needZeroFill && value < 10)
                append('0')
            append(value)
            append(separator)
            return true
        }
        return false
    }
}