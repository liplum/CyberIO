package net.liplum.common.persistence

import arc.util.io.Reads

/**
 * Wrap the [Reads] for some existed codes.
 *
 * Before reusing this, call [init].
 */
class CacheReaderWrapped : Reads(null) {
    /**
     * Initialized this preventing from null pointer
     */
    private var cached = CacheReaderSpec.X
    /**
     * Only used when this object need to be reused.
     * @return this
     */
    fun init(cache: CacheReaderSpec): CacheReaderWrapped {
        this.cached = cache
        return this
    }
    /**
     * Don't need to close this
     */
    override fun close() {}
    /**
     *  Don't use this. Please check whether existed codes used []
     */
    override fun checkEOF(): Int =
        cached.b().toInt()
    /** read long  */
    override fun l(): Long =
        cached.l()
    /** read int  */
    override fun i(): Int =
        cached.i()
    /** read short  */
    override fun s(): Short =
        cached.s()
    /** read unsigned short  */
    override fun us(): Int =
        cached.us()
    /** read byte  */
    override fun b(): Byte =
        cached.b()
    /** read byte array  */
    override fun b(length: Int): ByteArray =
        cached.b(length)
    /** read byte array  */
    override fun b(array: ByteArray): ByteArray =
        cached.b(array)
    /** read byte array w/ offset  */
    override fun b(array: ByteArray, offset: Int, length: Int): ByteArray =
        cached.b(array, offset, length)
    /** read unsigned byte  */
    override fun ub(): Int =
        cached.ub()
    /** read boolean  */
    override fun bool(): Boolean =
        cached.bool()
    /** read float  */
    override fun f(): Float =
        cached.f()
    /** read double  */
    override fun d(): Double =
        cached.d()
    /** read string (UTF)  */
    override fun str(): String =
        cached.str()
    /** skip bytes  */
    override fun skip(amount: Int) {
        cached.skip(amount)
    }

    companion object {
        /**
         * For globally reused, it's able to reused in a single thread app.
         */
        @JvmField
        val X = CacheReaderWrapped()
    }
}