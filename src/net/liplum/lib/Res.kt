package net.liplum.lib

import java.io.InputStream
import java.io.Reader
import java.nio.charset.Charset

class ResNotFoundException(msg: String) : RuntimeException(msg)
/**
 * Represents a location of resource.
 * Provides convenient methods of read.
 */
class Res(
    var locator: Class<*> = Res::class.java,
    var pos: String = "",
) {
    constructor(pos: String) :
            this(Res::class.java, pos = pos)

    fun readAsStream(): InputStream =
        load(locator, pos)
    /**
     * Create a reader from this resource location
     *
     * **Note**:  It is the caller's responsibility to close this reader.
     *
     * @return the reader
     */
    @JvmOverloads
    fun reader(charset: Charset = Charsets.UTF_8): Reader =
        load(locator, pos).reader(charset)
    /**
     * Use this resource location as a reader. It will automatically close the reader.
     */
    @JvmOverloads
    inline fun <R> useAsReader(
        use: (Reader) -> R,
        charset: Charset = Charsets.UTF_8
    ): R {
        load(locator, pos).reader(charset).use {
            return use(it)
        }
    }
    /**
     * Read all text from this resource location
     */
    fun readAllText(): String =
        load(locator, pos).reader().use { it.readText() }

    companion object {
        /**
         * @exception ResNotFoundException raises when this file doesn't exist.
         */
        @JvmStatic
        fun load(locator: Class<*>, name: String): InputStream =
            locator.getResourceAsStream(name) ?: throw ResNotFoundException("$name in ${locator.name}")
    }
}

