package net.liplum.utils

import arc.struct.OrderedSet
import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.math.PolarPos

fun Reads.intSet(): OrderedSet<Int> = RWU.readIntSet(this)
fun Writes.intSet(set: OrderedSet<Int>) = RWU.writeIntSet(this, set)
fun Reads.polarPos(): PolarPos = RWU.readPolarPos(this)
fun Writes.polarPos(pos: PolarPos) = RWU.writePolarPos(this, pos)