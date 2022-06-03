package net.liplum.api.cyber

import arc.util.pooling.Pool
import net.liplum.lib.Serialized
import net.liplum.mdt.utils.PackedPos
import plumy.pathkt.BFS
import plumy.pathkt.LinkedPath

class TransferTask {
    @Serialized
    var start: PackedPos = -1
    @Serialized
    var destination: PackedPos = -1
    /**
     * The routine will be calculated locally by [start] and [destination]
     */
    var routine: Path? = null
}

class Path : LinkedPath<INetworkNode>(), Pool.Poolable {
    override fun reset() {
        path.clear()
    }
}

class Pointer : BFS.IPointer<INetworkNode>, Pool.Poolable {
    override var previous: BFS.IPointer<INetworkNode>? = null
    override var self: INetworkNode = EmptyNetworkNode
    override fun reset() {
        previous = null
        self = EmptyNetworkNode
    }
}
