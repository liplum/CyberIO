package net.liplum.lib.persistence

import arc.struct.IntMap
import arc.struct.IntSeq
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.annotations.CacheRW
import net.liplum.lib.math.Polar
@CacheRW("net.liplum.lib.persistence")
fun IntSeq.read(reader: Reads): IntSeq {
    this.clear()
    val length = reader.i()
    for (i in 0 until length) {
        this.add(reader.i())
    }
    return this
}

@CacheRW("net.liplum.lib.persistence")
fun IntSeq.write(writer: Writes): IntSeq {
    writer.i(size)
    for (data in this.items) {
        writer.i(data)
    }
    return this
}

fun Reads.intSet(): OrderedSet<Int> = RWU.readIntSet(this)
fun Writes.intSet(set: OrderedSet<Int>) = RWU.writeIntSet(this, set)
fun Reads.polarPos(): Polar = RWU.readPolarPos(this)
fun Writes.polarPos(pos: Polar) = RWU.writePolarPos(this, pos)
fun <T> Reads.readSeq(howToRead: IHowToRead<T>): Seq<T> = RWU.readSeq(this, howToRead)
fun <T> Writes.writeSeq(seq: Seq<T>, howToWrite: IHowToWrite<T>) = RWU.writeSeq(this, seq, howToWrite)
fun <T> Reads.readIntMap(howToRead: IReadIntMap<T>): IntMap<T> = RWU.readIntMap(this, howToRead)
fun <T> Writes.writeIntMap(map: IntMap<T>, howToWrite: IWriteIntMap<T>) = RWU.writeIntMap(this, map, howToWrite)