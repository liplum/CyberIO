package net.liplum.data

import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.world.blocks.payloads.Payload
import net.liplum.lib.Serialized
import net.liplum.lib.persistence.*
import net.liplum.lib.utils.forLoop
import net.liplum.mdt.payload.EmptyPayload
import java.io.DataInputStream

typealias DataID = Int

const val EmptyDataID = Int.MIN_VALUE

class PayloadDataList(
    var capacity: Int = 1,
) : Iterable<PayloadData>, IRWableX {
    @Serialized
    var allData = Seq<PayloadData>()
    val isEmpty: Boolean
        get() = allData.isEmpty
    val canAddMore: Boolean
        get() = allData.size < capacity
    val size: Int
        get() = allData.size

    operator fun get(index: Int): PayloadData =
        allData[index]

    fun add(payload: PayloadData): Boolean {
        if (allData.size < capacity) {
            allData.add(payload)
            return true
        }
        return false
    }

    fun remove(payload: PayloadData): Boolean =
        allData.remove(payload)

    fun first(): PayloadData = allData.first()
    override fun iterator(): Iterator<PayloadData> =
        allData.iterator()

    fun indexPayload(payload: PayloadData): Int =
        allData.indexOf(payload, true)

    inline fun forLoop(func: (PayloadData) -> Unit) {
        allData.forLoop {
            func(it)
        }
    }

    fun hasData(id: DataID): Boolean {
        if (id == EmptyDataID) return false
        allData.forLoop {
            if (it.id == id) return true
        }
        return false
    }

    fun getData(id: DataID): PayloadData? {
        if (id == EmptyDataID) return null
        allData.forLoop {
            if (it.id == id) return it
        }
        return null
    }

    override fun read(reader: Reads) {
        allData.read(reader) {
            PayloadData(EmptyPayload, EmptyDataID).apply {
                read(reader)
            }
        }
    }

    override fun read(reader: DataInputStream) = CacheReaderSpec(reader).run {
        allData.read(this) {
            PayloadData(EmptyPayload, EmptyDataID).apply {
                read(this@run.cache)
            }
        }
        return@run
    }

    override fun write(writer: Writes) {
        allData.write(writer) {
            it.write(this)
        }
    }

    override fun write(writer: CacheWriter) {
        allData.write(writer) {
            it.write(this)
        }
    }
}

data class PayloadData(
    @Serialized
    var payload: Payload,
    @Serialized
    var id: DataID,
) : IRWableX {
    override fun read(reader: Reads) {
        id = reader.i()
        payload = Payload.read(reader)
    }

    override fun read(_reader_: DataInputStream) = CacheReaderSpec(_reader_).run {
        id = i()
        Warp {
            payload = Payload.read(this)
        }
    }

    override fun write(writer: Writes) {
        writer.i(id)
        Payload.write(payload, writer)
    }

    override fun write(writer: CacheWriter) {
        writer.i(id)
        writer.Wrap {
            Payload.write(payload, this)
        }
    }
}

val PayloadDataList.isNotEmpty: Boolean
    get() = !isEmpty