package net.liplum

import java.io.File

object FileSys {
    val RuntimeRoot: Dir by lazy {
        Dir(File(System.getProperty("user.dir"))).getOrCreate()
    }
    const val configFolderName = "config"
    val ConfigFolder: Dir by lazy {
        RuntimeRoot.subDir(configFolderName).getOrCreate()
    }
}
@JvmInline
value class F(val file: File) {
    val dir: Dir
        get() = Dir(file.parentFile)
    val exists: Boolean
        get() = file.exists()

    fun getOrCreate(): F {
        dir.tryCreate()
        if (!file.exists())
            file.createNewFile()
        return this
    }

    fun tryCreate() {
        if (!file.exists())
            file.mkdirs()
    }
}
@JvmInline
value class Dir(val folder: File) {
    fun subF(subPath: String) =
        F(File(folder, subPath))

    fun subDir(subPath: String) =
        Dir(File(folder, subPath))

    val exists: Boolean
        get() = folder.exists()

    fun getOrCreate(): Dir {
        if (!folder.exists())
            folder.mkdirs()
        return this
    }

    fun tryCreate() {
        if (!folder.exists())
            folder.mkdirs()
    }
}