package net.liplum.utils

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

/**
 * ZipUtil can extract files and subdirectories of a standard zip file to a destination directory.
 */
object ZipUtil {
    /**
     * Size of the buffer to read/write data
     */
    private const val BUFFER_SIZE = 4096
    fun unzip(zipFilePath: File, destDirectory: String) {
        File(destDirectory).run {
            if (!exists()) {
                mkdirs()
            }
        }

        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val filePath = destDirectory + File.separator + entry.name

                    if (!entry.isDirectory) {
                        // if the entry is a file, extracts it
                        extractFile(input, filePath)
                    } else {
                        // if the entry is a directory, make the directory
                        val dir = File(filePath)
                        dir.mkdir()
                    }
                }
            }
        }
    }

    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }
}