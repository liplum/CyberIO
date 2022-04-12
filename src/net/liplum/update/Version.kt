package net.liplum.update

data class Version(val major: Int, val minor: Int) {
    operator fun compareTo(b: Version): Int {
        if (major > b.major)
            return 1
        if (major < b.major)
            return -1
        return when {
            minor > b.minor -> 1
            minor == b.major -> 0
            else -> -1
        }
    }
}