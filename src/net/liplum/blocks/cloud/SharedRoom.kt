package net.liplum.blocks.cloud

import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.world.modules.ItemModule
import net.liplum.persistance.IRWable

open class SharedRoom : IRWable {
    var usersPos: Seq<Int>
    var sharedItemModule: ItemModule? = null

    constructor() {
        usersPos = Seq()
    }

    constructor(usersPos: Seq<Int>) {
        this.usersPos = usersPos
    }

    open fun online(sharedBuild: IShared) {
        if (sharedItemModule == null) {
            sharedItemModule = sharedBuild.sharedItems
        } else {
            sharedBuild.sharedItems = sharedItemModule!!
        }
    }

    open fun checkExist(isExisted: (Int) -> Boolean) {
        val it = usersPos.iterator()
        while (it.hasNext()) {
            val cur = it.next()
            if (!isExisted(cur)) {
                it.remove()
            }
        }
    }

    override fun read(reader: Reads) {
        val len = reader.i()
        val seq = Seq<Int>(len)
        for (i in 0..len) {
            seq.add(i);
        }
        usersPos = seq
    }

    override fun write(writer: Writes) {
        writer.i(usersPos.size)
        for (pos in usersPos) {
            writer.i(pos)
        }
    }

    companion object {
        @JvmStatic
        fun <Owner : Building> readFrom(reader: Reads): SharedRoom {
            val len = reader.i()
            val seq = Seq<Int>(len)
            for (i in 0..len) {
                seq.add(i);
            }
            return SharedRoom(seq)
        }
    }
}