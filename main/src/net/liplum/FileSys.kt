package net.liplum

import net.liplum.common.Dir
import java.io.File

object FileSys {
    val RuntimeRoot: Dir by lazy {
        Dir(File(System.getProperty("user.dir"))).getOrCreate()
    }
    val ConfigFolder: Dir by lazy {
        RuntimeRoot.subDir("config").getOrCreate()
    }
    val CyberIoFolder: Dir by lazy {
        ConfigFolder.subDir("cyberio").getOrCreate()
    }
}

