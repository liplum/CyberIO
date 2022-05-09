package net.liplum.update

data class Version2(val major: Int, val minor: Int) {
    operator fun compareTo(b: Version2): Int {
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

    override fun toString() = "$major.$minor"
    class VersionParseException(msg: String) : RuntimeException(msg)
    companion object {
        @JvmField val Zero = Version2(0, 0)
        @JvmStatic
        fun valueOf(str: String): Version2 {
            val parts = str.split('.')
            if (parts.size < 2) throw VersionParseException("$str has less than 2 parts")
            try {
                val major = parts[0].toInt()
                val minor = parts[1].toInt()
                return Version2(major, minor)
            } catch (e: Exception) {
                throw VersionParseException("Can't parse value from $str")
            }
        }
    }
}