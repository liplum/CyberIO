package net.liplum.blocks.prism

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.lib.TR
import net.liplum.lib.math.Polar
import net.liplum.lib.persistence.CacheReaderSpec
import net.liplum.lib.persistence.CacheWriter
import net.liplum.lib.persistence.IRWable
import net.liplum.lib.utils.isOn
import net.liplum.lib.utils.off
import net.liplum.lib.utils.on
import net.liplum.mdt.ClientOnly
import java.io.DataInputStream

class Crystal : IRWable {
    var revolution = Polar()
    var rotation = Polar()
    @ClientOnly
    var img = TR()
    var orbitPos = 0
    var data = 0
    var isClockwise: Boolean
        get() = data.isOn(ClockwisePos)
        set(clockwise) {
            data = if (clockwise) data.on(ClockwisePos) else data.off(ClockwisePos)
        }
    var isRemoved: Boolean
        get() = data.isOn(RemovedPos)
        set(removed) {
            if (removed) {
                data = data.on(RemovedPos)
                isAwaitAdding = false
            } else data = data.off(RemovedPos)
        }
    var isAwaitAdding: Boolean
        get() = data.isOn(AwaitAddingPos)
        set(awaitAdding) {
            if (awaitAdding) {
                data = data.on(AwaitAddingPos)
                isRemoved = false
            } else data = data.off(AwaitAddingPos)
        }

    companion object {
        private const val ClockwisePos = 0
        private const val RemovedPos = 1
        private const val AwaitAddingPos = 2
        private const val Pos4 = 3
        private const val Pos5 = 4
        private const val Pos6 = 5
        private const val Pos7 = 6
        private const val Pos8 = 7
    }

    override fun read(reader: Reads) = reader.run {
        orbitPos = b().toInt()
        revolution.read(this)
        rotation.read(this)
        data = b().toInt()
    }

    override fun read(reader: DataInputStream) = CacheReaderSpec(reader).run {
        orbitPos = b().toInt()
        revolution.read(reader)
        rotation.read(reader)
        data = b().toInt()
    }

    override fun write(writer: Writes) = writer.run {
        b(orbitPos)
        revolution.write(this)
        rotation.write(this)
        b(data)
    }

    override fun write(writer: CacheWriter) = writer.run {
        b(orbitPos)
        revolution.write(this)
        rotation.write(this)
        b(data)
    }
}