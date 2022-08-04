package net.liplum.common.math

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.common.persistence.CacheReaderSpec
import net.liplum.common.persistence.CacheWriter
import net.liplum.common.persistence.IRWableX
import plumy.core.math.Polar
import java.io.DataInputStream

/**
 * It represents a polar coordinate using radian.
 */
class PolarX(
    r: Float = 0f,
    a: Float = 0f,
) : Polar(r, a), IRWableX {
    override fun read(reader: Reads) {
        r = reader.f()
        a = reader.f()
    }

    override fun read(reader: DataInputStream) = CacheReaderSpec(reader).run {
        r = f()
        a = f()
    }

    override fun write(writer: Writes) {
        writer.f(r)
        writer.f(a)
    }

    override fun write(writer: CacheWriter) {
        writer.f(r)
        writer.f(a)
    }
}
