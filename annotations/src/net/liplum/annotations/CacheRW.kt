package net.liplum.annotations

/**
 * It will automatically generate another version read/write for CacheReader/CacheWriter.
 */
annotation class CacheRW(
    /**
     * The file where the `mirror` function locates.
     */
    val fileName: String,
    /**
     * The package where the `mirror` function locates.
     * The same package as default
     */
    val packageName: String = ""
)