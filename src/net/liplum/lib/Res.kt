package net.liplum.lib

import java.io.InputStream
import java.io.Reader
import java.nio.charset.Charset

class ResNotFoundException(msg: String) : RuntimeException(msg)
class Res(
    var locator: Class<*> = Res::class.java,
    var pos: String = "",
) {
    constructor(pos: String) :
            this(Res::class.java, pos = pos)

    fun readStream(): InputStream =
        load(locator, pos)
    @JvmOverloads
    fun reader(charset: Charset = Charsets.UTF_8): Reader =
        load(locator, pos).reader(charset)

    companion object {
        /**
         * @exception ResNotFoundException raises when this file doesn't exist.
         */
        @JvmStatic
        fun load(locator: Class<*>, name: String): InputStream =
            locator.getResourceAsStream(name) ?: throw ResNotFoundException("$name in ${locator.name}")
    }
}

