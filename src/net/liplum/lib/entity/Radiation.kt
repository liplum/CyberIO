package net.liplum.lib.entity

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.persistance.IRWable

open class Radiation(var range: Float = 0f) : IRWable {
    override fun read(reader: Reads) {
        range = reader.f()
    }

    override fun write(writer: Writes) {
        writer.f(range)
    }
}

class PosRadiation(
    range: Float = 0f,
    var x: Float = 0f,
    var y: Float = 0f
) : Radiation(range) {
    override fun read(reader: Reads) {
        super.read(reader)
        x = reader.f()
        y = reader.f()
    }

    override fun write(writer: Writes) {
        super.write(writer)
        writer.f(x)
        writer.f(y)
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
