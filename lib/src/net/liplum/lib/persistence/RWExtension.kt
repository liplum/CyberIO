@file:CacheRW

package net.liplum.lib.persistence

import arc.math.geom.Vec2
import arc.struct.*
import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.annotations.CacheRW
import net.liplum.lib.utils.forEach

fun IntSeq.read(reader: Reads): IntSeq {
    this.clear()
    val length = reader.i()
    for (i in 0 until length) {
        this.add(reader.i())
    }
    return this
}

fun IntSeq.write(writer: Writes): IntSeq {
    writer.i(size)
    for (data in this.items) {
        writer.i(data)
    }
    return this
}

fun FloatSeq.read(reader: Reads): FloatSeq {
    this.clear()
    val length = reader.i()
    for (i in 0 until length) {
        this.add(reader.f())
    }
    return this
}

fun FloatSeq.write(writer: Writes): FloatSeq {
    writer.i(size)
    for (data in this.items) {
        writer.f(data)
    }
    return this
}

fun IntSet.read(reader: Reads): IntSet {
    this.clear()
    val length = reader.i()
    for (i in 0 until length) {
        this.add(reader.i())
    }
    return this
}
/**
 * It calls the [IntSet.iterator] function. Note this can't work in multi-thread or nested calling.
 */
fun IntSet.write(writer: Writes): IntSet {
    writer.i(size)
    this.forEach {
        writer.i(it)
    }
    return this
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

fun Vec2.read(reader: Reads): Vec2 {
    x = reader.f()
    y = reader.f()
    return this
}

fun Vec2.write(writer: Writes): Vec2 {
    writer.f(x)
    writer.f(y)
    return this
}