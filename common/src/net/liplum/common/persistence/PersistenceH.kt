@file:JvmName("PersistenceH")

package net.liplum.common.persistence

import arc.util.io.Reads
import arc.util.io.Writes

val CacheWriterX = CacheWriter()
val CacheReaderWrapX = CacheReaderWrapped()
val CacheWriterWrapX = CacheWriterWrapped()
inline fun WriteIntoCache(
    write: Writes,
    revision: Int,
    writing: CacheWriter.() -> Unit,
) {
    CacheWriterX.init().apply {
        writing()
        flushAll(write, revision)
    }
}

inline fun CacheWriter.Wrap(
    writing: CacheWriterWrapped.() -> Unit,
) {
    CacheWriterWrapX.init(this).apply(writing)
}

inline fun ReadFromCache(
    read: Reads,
    revision: Int,
    reading: CacheReaderSpec.() -> Unit,
) {
    CacheReader.startRead(read, revision) {
        reading()
    }
}

inline fun CacheReaderSpec.Warp(
    reading: CacheReaderWrapped.() -> Unit,
) {
    CacheReaderWrapX.init(this).apply(reading)
}
