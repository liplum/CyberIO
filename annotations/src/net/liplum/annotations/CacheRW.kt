package net.liplum.annotations

/**
 * It will automatically generate a mirror of current file but using read/write for CacheReader/CacheWriter.
 */
@Target(AnnotationTarget.FILE)
annotation class CacheRW(
    /**
     * The mirror file name, empty as default which represents the mirror file's name is based on annotated file.
     */
    val fileName: String = "",
)