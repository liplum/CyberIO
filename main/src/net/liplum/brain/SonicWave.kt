package net.liplum.brain

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.common.entity.PosRadiation
import net.liplum.common.entity.Queue
import net.liplum.common.persistence.CacheReaderSpec
import net.liplum.common.persistence.CacheWriter
import java.io.DataInputStream

class SonicWave(
    range: Float = 0f,
    x: Float = 0f,
    y: Float = 0f,
    var damage: Float = 0f,
) : PosRadiation(range, x, y) {
    override fun read(reader: Reads) {
        super.read(reader)
        damage = reader.f()
    }

    override fun write(writer: Writes) {
        super.write(writer)
        writer.f(damage)
    }

    override fun read(reader: DataInputStream) = CacheReaderSpec(reader).run {
        super.read(reader)
        damage = f()
    }

    override fun write(writer: CacheWriter) {
        super.write(writer)
        writer.f(damage)
    }

    companion object {
        @JvmStatic
        fun readEmpty(reader: Reads) {
            PosRadiation.readEmpty(reader)
            reader.f()
        }
    }
}

fun SonicWaveQueue(size: Int = 1) =
    Queue(size, ::SonicWave)
