package net.liplum.update

data class Version2(val major: Int, val minor: Int) : Comparable<Any> {
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

    fun equalsString(versionString: String) =
        this == tryValueOf(versionString)

    override fun compareTo(other: Any): Int =
        when (other) {
            is String -> this.compareTo(valueOf(other))
            is Version2 -> this.compareTo(other)
            is Int -> this.compareTo(Version2(other, 0))
            is Byte -> this.compareTo(Version2(other.toInt(), 0))
            is Short -> this.compareTo(Version2(other.toInt(), 0))
            else -> throw IllegalArgumentException("$this can't compare to $other")
        }

    override fun toString() = "$major.$minor"
    class VersionParseException(msg: String) : RuntimeException(msg)
    companion object {
        @JvmField val Zero = Version2(0, 0)
        @JvmStatic
        fun valueOf(str: String): Version2 {
            val parts = str.split('.')
            if (parts.isEmpty()) throw VersionParseException("$str doesn't have any version info.")
            try {
                return if (parts.size == 1) {
                    val major = parts[0].toInt()
                    Version2(major, 0)
                } else {
                    val major = parts[0].toInt()
                    val minor = parts[1].toInt()
                    Version2(major, minor)
                }
            } catch (e: Exception) {
                throw VersionParseException("Can't parse value from $str")
            }
        }
        @JvmStatic
        fun tryValueOf(str: String): Version2? {
            val parts = str.split('.')
            if (parts.isEmpty()) throw VersionParseException("$str doesn't have any version info.")
            return try {
                if (parts.size == 1) {
                    val major = parts[0].toInt()
                    Version2(major, 0)
                } else {
                    val major = parts[0].toInt()
                    val minor = parts[1].toInt()
                    Version2(major, minor)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}