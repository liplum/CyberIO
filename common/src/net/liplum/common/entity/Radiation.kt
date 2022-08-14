package net.liplum.common.entity

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.common.persistence.CacheReaderSpec
import net.liplum.common.persistence.CacheWriter
import net.liplum.common.persistence.IRWableX
import java.io.DataInputStream

open class Radiation(
    @JvmField
    var range: Float = 0f,
) : IRWableX {
    override fun read(reader: Reads) {
        range = reader.f()
    }

    override fun read(reader: DataInputStream) = CacheReaderSpec(reader).run {
        range = f()
    }

    override fun write(writer: Writes) {
        writer.f(range)
    }

    override fun write(writer: CacheWriter) {
        writer.f(range)
    }

    companion object {
        @JvmStatic
        fun readEmpty(reader: Reads) {
            reader.f()
        }
    }
}

open class PosRadiation(
    range: Float = 0f,
    @JvmField
    var x: Float = 0f,
    @JvmField
    var y: Float = 0f,
) : Radiation(range) {
    override fun read(reader: Reads) {
        super.read(reader)
        x = reader.f()
        y = reader.f()
    }

    override fun read(reader: DataInputStream) = CacheReaderSpec(reader).run {
        super.read(reader)
        x = f()
        y = f()
    }

    override fun write(writer: Writes) {
        super.write(writer)
        writer.f(x)
        writer.f(y)
    }

    override fun write(writer: CacheWriter) {
        super.write(writer)
        writer.f(x)
        writer.f(y)
    }

    companion object {
        @JvmStatic
        fun readEmpty(reader: Reads) {
            Radiation.readEmpty(reader)
            reader.f()
            reader.f()
        }
    }
}

fun RadiationArray(size: Int = 1) =
    FixedList(size, ::Radiation)

fun PosRadiationArray(size: Int = 1) =
    FixedList(size, ::PosRadiation)

fun RadiationArray(size: Int = 1, init: (Int, Radiation) -> Unit) =
    FixedList(size, ::Radiation, init)

fun PosRadiationArray(size: Int = 1, init: (Int, PosRadiation) -> Unit) =
    FixedList(size, ::PosRadiation, init)

fun RadiationQueue(size: Int = 1) =
    Queue(size, ::Radiation)

fun RadiationQueue(size: () -> Int = { 1 }) =
    Queue(size, ::Radiation)

fun PosRadiationQueue(size: Int = 1) =
    Queue(size, ::PosRadiation)
