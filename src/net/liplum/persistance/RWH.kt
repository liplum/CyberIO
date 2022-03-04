package net.liplum.persistance

import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.math.PolarPos

fun Reads.intSet(): OrderedSet<Int> = RWU.readIntSet(this)
fun Writes.intSet(set: OrderedSet<Int>) = RWU.writeIntSet(this, set)
fun Reads.polarPos(): PolarPos = RWU.readPolarPos(this)
fun Writes.polarPos(pos: PolarPos) = RWU.writePolarPos(this, pos)
fun <T> Reads.readSeq(howToRead: IHowToRead<T>): Seq<T> = RWU.readSeq(this, howToRead)
fun <T> Writes.writeSeq(seq: Seq<T>, howToWrite: IHowToWrite<T>) = RWU.writeSeq(this, seq, howToWrite)