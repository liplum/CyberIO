package net.liplum.api.cyber

import arc.struct.IntSeq
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.world.modules.BlockModule
import net.liplum.data.DataNetwork

class NetworkModule : BlockModule() {
    var network = DataNetwork()
    var links = IntSeq()
    var init = false
    override fun write(write: Writes) {}
    override fun read(read: Reads) {}
}
