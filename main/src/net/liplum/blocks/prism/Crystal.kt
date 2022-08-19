package net.liplum.blocks.prism

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.Var
import net.liplum.common.math.PolarX
import net.liplum.common.persistence.CacheReaderSpec
import net.liplum.common.persistence.CacheWriter
import net.liplum.common.persistence.IRWableX
import net.liplum.common.util.isOn
import net.liplum.common.util.off
import net.liplum.common.util.on
import plumy.core.ClientOnly
import plumy.core.assets.TR
import java.io.DataInputStream

class Crystal : IRWableX {
    var revolution = PolarX()
    var rotation = PolarX()
    @ClientOnly
    var img = TR()
    @ClientOnly
    var lastPassThroughTime = Var.PrismCrystalPassThroughBloomTime
    val isActivated get() = lastPassThroughTime < Var.PrismCrystalPassThroughBloomTime
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
