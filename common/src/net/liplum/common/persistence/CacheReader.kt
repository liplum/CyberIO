package net.liplum.common.persistence

import arc.util.io.Reads
import arc.util.io.Writes
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

object CacheReader {
    inline fun startRead(
        read: Reads,
        revision: Int,
        reading: CacheReaderSpec.() -> Unit,
    ) {
        // Read the revision number
        val readRevision = read.i()
        // Read the total length of blocks, don't worry it can represent about 2 GB
        val blockLength = read.i()
        // If revision number doesn't match, skip all
        if (readRevision != revision) {
            read.skip(blockLength)
        } else {
            // If revision number match, read all into cache
            val data = read.b(blockLength)
            val cache = ByteArrayInputStream(data)
            val reader = DataInputStream(cache)
            // Byte array input stream doesn't need to be closed
            CacheReaderSpec(reader).reading()
        }
    }
}
@JvmInline
value class CacheReaderSpec(val cache: DataInputStream) {
    /** read long  */
    fun l(): Long =
        cache.readLong()
    /** read int  */
    fun i(): Int =
        cache.readInt()
    /** read short  */
    fun s(): Short =
        cache.readShort()
    /** read unsigned short  */
    fun us(): Int =
        cache.readUnsignedShort()
    /** read byte  */
    fun b(): Byte =
        cache.readByte()
    /** allocate & read byte array  */
    fun b(length: Int): ByteArray = ByteArray(length).apply {
        cache.read(this)
    }
    /** read byte array  */
    fun b(array: ByteArray): ByteArray = array.apply {
        cache.read(array)
    }
    /** read byte array w/ offset  */
    fun b(array: ByteArray, offset: Int, length: Int): ByteArray = array.apply {
        cache.read(array, offset, length)
    }
    /** read unsigned byte  */
    fun ub(): Int =
        cache.readUnsignedByte()
    /** read boolean  */
    fun bool(): Boolean =
        cache.readBoolean()
    /** read float  */
    fun f(): Float =
        cache.readFloat()
    /** read double  */
    fun d(): Double =
        cache.readDouble()
    /** read string (UTF)  */
    fun str(): String =
        cache.readUTF()
    /** skip bytes  */
    fun skip(amount: Int) =
        cache.skipBytes(amount)

    companion object {
        val X = CacheReaderSpec(DataInputStream(ByteArrayInputStream(ByteArray(0))))
    }
}
/**
 * Before reusing this, call [init].
 */
class CacheWriter {
    private var cache = ByteArrayOutputStream()
    private var writer = DataOutputStream(cache)
    /**
     * Create two objects, output stream and its writer.
     * Only used when this object need to be reused.
     * @return this
     */
    fun init(): CacheWriter {
        // Byte array output stream doesn't need to be closed
        cache = ByteArrayOutputStream()
        writer = DataOutputStream(cache)
        return this
    }

    fun flushAll(writeIn: Writes, reverison: Int) {
        // Flush
        writer.flush()
        // Write the revision number
        writeIn.i(reverison)
        // Write the total length for skipping
        val blockLength = cache.size()
        writeIn.i(blockLength)//Don't worry it can represent about 2 GB
        // Write all bytes
        val allBytes = cache.toByteArray()
        writeIn.b(allBytes)
    }
    /** write long  */
    fun l(i: Long) {
        writer.writeLong(i)
    }
    /** write int  */
    fun i(i: Int) {
        writer.writeInt(i)
    }
    /** write byte  */
    fun b(i: Int) {
        writer.writeByte(i)
    }
    /** write bytes  */
    fun b(array: ByteArray, offset: Int, length: Int) {
        writer.write(array, offset, length)
    }
    /** write bytes  */
    fun b(array: ByteArray) {
        b(array, 0, array.size)
    }
    /** write boolean (writes a byte internally)  */
    fun bool(b: Boolean) {
        b(if (b) 1 else 0)
    }
    /** write short  */
    fun s(i: Int) {
        writer.writeShort(i)
    }
    /** write float  */
    fun f(f: Float) {
        writer.writeFloat(f)
    }
    /** write double  */
    fun d(d: Double) {
        writer.writeDouble(d)
    }
    /** writes a string (UTF)  */
    fun str(str: String) {
        writer.writeUTF(str)
    }

    companion object {
        /**
         * For globally reused, it's able to reused in a single thread app.
         */
        @JvmField
        val X = CacheWriter()
    }
}