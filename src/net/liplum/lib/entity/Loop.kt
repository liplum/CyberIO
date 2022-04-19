package net.liplum.lib.entity

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.persistance.IRWable

class Loop(progress: Float = 0f) : IRWable {
    var progress: Float = progress
        set(value) {
            field = value % 1f
        }

    override fun read(reader: Reads) {
        progress = reader.f()
    }

    override fun write(writer: Writes) {
        writer.f(progress)
    }
}