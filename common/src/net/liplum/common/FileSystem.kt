package net.liplum.common

import arc.files.Fi
import arc.util.io.Streams
import java.io.File

@JvmInline
value class F(val file: File) {
    constructor(path: String) : this(File(path))

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

    fun getOrCreate(init: String): F {
        dir.tryCreate()
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(init)
        }
        return this
    }

    inline fun getOrCreate(init: () -> String, onCreated: () -> Unit): F {
        dir.tryCreate()
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(init())
            onCreated()
        }
        return this
    }

    fun delete(): F {
        file.delete()
        return this
    }

    fun overwrite(text: String): F {
        file.writeText(text)
        return this
    }

    fun getOrCreate(init: ByteArray): F {
        dir.tryCreate()
        if (!file.exists()) {
            file.createNewFile()
            file.writeBytes(init)
        }
        return this
    }

    fun tryCreate() {
        if (!file.exists())
            file.mkdirs()
    }
}

fun F.toFi(): Fi =
    Fi(file)
@JvmInline
value class Dir(val folder: File) {
    constructor(path: String) : this(File(path))

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

fun File.replaceBy(
    bytes: ByteArray,
) {
    val fos = this.outputStream()
    Streams.copy(
        bytes.inputStream(),
        fos
    )
    fos.close()
}

fun File.replaceBy(
    replacement: File,
) {
    val ins = replacement.inputStream()
    val fos = this.outputStream()
    Streams.copy(
        ins,
        fos
    )
    ins.close()
    fos.close()
}