package net.liplum.common.entity

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.common.persistence.CacheReaderSpec
import net.liplum.common.persistence.CacheWriter
import net.liplum.common.persistence.IRWableX
import java.io.DataInputStream

open class Progress(progress: Float = 0f) : IRWableX {
    open var progress: Float = progress
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    override fun read(reader: Reads) {
        progress = reader.f()
    }

    override fun read(reader: DataInputStream) = CacheReaderSpec(reader).run {
        progress = f()
    }

    override fun write(writer: Writes) {
        writer.f(progress)
    }

    override fun write(writer: CacheWriter) {
        writer.f(progress)
    }

    override fun toString() = "$progress%"
}

class Loop(progress: Float = 0f) : Progress(progress) {
    override var progress: Float = progress
        set(value) {
            field = value % 1f
        }
}
