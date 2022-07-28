package net.liplum.common.persistence

import arc.util.io.Writes

/**
 * Wrap the [Writes] for some existed codes.
 *
 * Before reusing this, call [init].
 */
class CacheWriterWrapped : Writes(null) {
    /**
     * Initialized this preventing from null pointer
     */
    var cached = CacheWriter.X
    /**
     * Only used when this object need to be reused.
     * @return this
     */
    fun init(cacheWriter: CacheWriter): CacheWriterWrapped {
        cached = cacheWriter
        return this
    }
    /**
     * Don't need to close this
     */
    override fun close() {}
    override fun l(i: Long) =
        cached.l(i)

    override fun i(i: Int) =
        cached.i(i)

    override fun b(i: Int) =
        cached.b(i)

    override fun b(array: ByteArray, offset: Int, length: Int) =
        cached.b(array, offset, length)

    override fun b(array: ByteArray) =
        cached.b(array)

    override fun bool(b: Boolean) =
        cached.bool(b)

    override fun s(i: Int) =
        cached.s(i)

    override fun f(f: Float) =
        cached.f(f)

    override fun d(d: Double) =
        cached.d(d)

    override fun str(str: String) =
        cached.str(str)
}