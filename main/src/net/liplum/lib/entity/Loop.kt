package net.liplum.lib.entity

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.lib.persistance.IRWable

open class Progress(progress: Float = 0f) : IRWable {
    open var progress: Float = progress
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    override fun read(reader: Reads) {
        progress = reader.f()
    }

    override fun write(writer: Writes) {
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
