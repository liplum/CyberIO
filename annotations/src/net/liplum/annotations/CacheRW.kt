package net.liplum.annotations

/**
 * It will automatically generate another version read/write for CacheReader/CacheWriter.
 */
annotation class CacheRW(
    val packageName: String
)