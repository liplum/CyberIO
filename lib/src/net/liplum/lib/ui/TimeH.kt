package net.liplum.lib.ui

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
        seperator: Char = ':',
    ): String {
        val days = sec / SecPreDay
        val hours = (sec % SecPreDay) / SecPreHour
        val mins = ((sec % SecPreDay) % SecPreHour) / SecPreMin
        val secs = ((sec % SecPreDay) % SecPreHour) % SecPreMin
        return toTimeString(days, hours, mins, secs, seperator)
    }
    @JvmStatic
    @JvmOverloads
    fun toTimeString(
        day: Int = 0,
        hour: Int = 0,
        min: Int = 0,
        sec: Int = 0,
        seperator: Char = ':',
    ): String {
        val sb = StringBuilder()
        val dayAdded = sb.tryAppendTimeFragment(day, seperator, false)
        val hourAdded = sb.tryAppendTimeFragment(hour, seperator, dayAdded)
        val minAdded = sb.tryAppendTimeFragment(min, seperator, hourAdded)
        if (minAdded && sec < 10)
            sb.append('0')
        sb.append(sec)

        return sb.toString()
    }
    @JvmStatic
    private fun StringBuilder.tryAppendTimeFragment(
        value: Int, seperator: Char, needZeroFill: Boolean = true,
    ): Boolean {
        if (value > 0) {
            if (needZeroFill && value < 10)
                append('0')
            append(value)
            append(seperator)
            return true
        }
        return false
    }
}
