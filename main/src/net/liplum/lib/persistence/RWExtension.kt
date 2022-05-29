@file:JvmName("RWExtension")

package net.liplum.lib.persistence

import arc.struct.*
import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.lib.utils.forEach

fun IntSeq.read(reader: Reads): IntSeq {
// Start
    this.clear()
    val length = reader.i()
    for (i in 0 until length) {
        this.add(reader.i())
    }
    return this
// End
}

fun IntSeq.write(writer: Writes): IntSeq {
// Start
    writer.i(size)
    for (data in this.items) {
        writer.i(data)
    }
    return this
// End
}

fun FloatSeq.read(reader: Reads): FloatSeq {
// Start
    this.clear()
    val length = reader.i()
    for (i in 0 until length) {
        this.add(reader.f())
    }
    return this
// End
}

fun FloatSeq.write(writer: Writes): FloatSeq {
// Start
    writer.i(size)
    for (data in this.items) {
        writer.f(data)
    }
    return this
// End
}

fun IntSet.read(reader: Reads): IntSet {
// Start
    this.clear()
    val length = reader.i()
    for (i in 0 until length) {
        this.add(reader.i())
    }
    return this
// End
}
/**
 * It calls the [IntSet.iterator] function. Note this can't work in multi-thread or nested calling.
 */
fun IntSet.write(writer: Writes): IntSet {
// Start
    writer.i(size)
    this.forEach {
        writer.i(it)
    }
    return this
// End
}

fun ObjectSet<Int>.read(reader: Reads): ObjectSet<Int> {
    this.clear()
    val length: Int = reader.i()
    for (i in 0 until length) {
        add(reader.i())
    }
    return this
}

fun ObjectSet<Int>.write(writer: Writes): ObjectSet<Int> {
    writer.i(this.size)
    for (data in this) {
        writer.i(data)
    }
    return this
}

inline fun <reified C, reified T> C.read(
    reader: Reads, reading: Reads.() -> T
): C where C : ObjectSet<T> {
    this.clear()
    val length: Int = reader.i()
    for (i in 0 until length) {
        add(reader.reading())
    }
    return this
}

inline fun <reified C, reified T> C.write(
    writer: Writes, writing: Writes.(T) -> Unit
): C where C : ObjectSet<T> {
    writer.i(this.size)
    for (data in this) {
        writer.writing(data)
    }
    return this
}

inline fun <reified C, reified T> C.read(
    reader: Reads, reading: Reads.() -> T
): C where C : Seq<T> {
    this.clear()
    val length: Int = reader.i()
    for (i in 0 until length) {
        add(reader.reading())
    }
    return this
}

inline fun <reified C, reified T> C.write(
    writer: Writes, writing: Writes.(T) -> Unit
): C where C : Seq<T> {
    writer.i(this.size)
    for (data in this) {
        writer.writing(data)
    }
    return this
}
