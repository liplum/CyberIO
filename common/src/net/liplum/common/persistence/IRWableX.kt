package net.liplum.common.persistence

import arc.util.io.Reads
import arc.util.io.Writes
import java.io.DataInputStream

interface IRWable {
    fun read(reader: Reads)
    fun write(writer: Writes)
}

interface IRWableX : IRWable {
    fun read(reader: DataInputStream)
    fun write(writer: CacheWriter)
}