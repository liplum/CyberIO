package net.liplum.api.cyber

import arc.struct.IntSet
import arc.util.pooling.Pool
import arc.util.pooling.Pools
import mindustry.world.blocks.payloads.Payload
import net.liplum.CLog
import net.liplum.data.PayloadDataList
import net.liplum.lib.Serialized
import net.liplum.lib.utils.coherentApply
import net.liplum.mdt.utils.PackedPos
import plumy.pathkt.BFS
import plumy.pathkt.EasyBFS
import plumy.pathkt.LinkedPath
import plumy.pathkt.findPath
import java.util.*

class DataNetwork {
    val entity = DataNetworkUpdater.create()
    val nodes = ArrayList<INetworkNode>()
    val routineCache = HashMap<Any, Path>()
    var id = lastNetworkID++
        private set

    fun onNetworkNodeChanged() {
        clearRoutineCache()
    }

    fun update() {
    }
    /**
     * Post a data transfer request
     * @param applicant who need the data. Destination of data transfer.
     * @param subject who has the data. Start point of data transfer.
     * @param application the data info
     */
    fun postRequest(
        applicant: INetworkNode,
        subject: INetworkNode,
        application: Payload,
    ) {
        if (subject == applicant) return // Don't send to self
        if (applicant.inTheSameNetwork(subject)) { // Only can send data in the same network
            val path = findPath(subject, applicant) ?: return // impossible not to find a path
            if (path.start != subject || path.destination != applicant) return // Can't find the correct way
            val startPos = subject.building.pos()
            val destPost = applicant.building.pos()
            coherentApply(subject, applicant) {
                transferTask.let {
                    it.start = startPos
                    it.destination = destPost
                    it.routine = path
                    path.bind(it)
                }
            }
            subject.transferTask.curData = application
        }
    }
    /**
     * Find a path from [start] to [destination].
     * Caller should ensure the path exists.
     */
    fun findPath(start: INetworkNode, destination: INetworkNode): Path? {
        val toKey = genStart2DestinationKey(start, destination)
        val cached = routineCache[toKey]
        if (cached != null) return cached
        val fromKey = genStart2DestinationKey(destination, start)
        bfs.findPath(start, destination) { path ->
            routineCache[toKey] = path
            routineCache[fromKey] = path.reversedPath()
            return path
        }
        return null
    }

    fun remove() {
        entity.remove()
        nodes.clear()
        clearRoutineCache()
    }

    private fun clearRoutineCache() {
        routineCache.forEach { (_, path) ->
            path.tryDeconstruct()
        }
        routineCache.clear()
    }

    fun addNetwork(other: DataNetwork) {
        if (other == this) return
        other.entity.remove()
        other.nodes.forEach(::add)
    }

    fun add(node: INetworkNode) {
        if (node.network != this || !node.init) {
            node.network = this
            node.init = true
            nodes.add(node)
            entity.add()
            onNetworkNodeChanged()
        }
    }

    fun merge(node: INetworkNode) {
        if (node.network == this) return
        node.network.entity.remove()
        // iterate its link
        entity.add()
        bfsQueue.clear()
        bfsQueue.addLast(node)
        closedSet.clear()
        while (bfsQueue.size > 0) {
            val child = bfsQueue.removeFirst()
            add(child)
            for (next in child.linkedVertices) {
                if (closedSet.add(next.building.pos())) {
                    bfsQueue.addLast(next)
                }
            }
        }
        onNetworkNodeChanged()
    }

    fun reflow(node: INetworkNode) {
        bfsQueue.clear()
        bfsQueue.addLast(node)
        closedSet.clear()
        while (bfsQueue.size > 0) {
            val child = bfsQueue.removeFirst()
            add(child)
            for (next in child.linkedVertices) {
                if (closedSet.add(next.building.pos())) {
                    bfsQueue.addLast(next)
                }
            }
        }
        onNetworkNodeChanged()
    }

    inline fun forEachDataIndexed(func: (Int, INetworkNode, Payload) -> Unit) {
        var i = 0
        for (node in nodes) {
            for (data in node.dataList) {
                func(i, node, data)
                i++
            }
        }
    }

    inline fun forEachData(func: (INetworkNode, Payload) -> Unit) {
        for (node in nodes) {
            for (data in node.dataList)
                func(node, data)
        }
    }

    override fun toString() =
        "DataNetwork#$id"

    companion object {
        /**
         * Only used in pathfinder. it can be safely removed when reset.
         */
        val pointerPool: Pool<Pointer> = Pools.get(Pointer::class.java, ::Pointer)
        /**
         * It will
         */
        val pathPool: Pool<Path> = Pools.get(Path::class.java, ::Path)
        private val bfs = EasyBFS<INetworkNode, Path>(
            { pointerPool.obtain() },
            { pathPool.obtain() },
        ).apply {
            clearSeen = {
                seen.forEach { pointerPool.free(it as Pointer) }
                seen.clear()
            }
        }
        private val tmp1 = ArrayList<INetworkNode>()
        private val bfsQueue = LinkedList<INetworkNode>()
        private val closedSet = IntSet()
        @JvmStatic
        private var lastNetworkID = 0
        fun genStart2DestinationKey(start: INetworkNode, dest: INetworkNode): Any =
            start.building.id * 31 + dest.building.id
    }
}

class TransferTask {
    @Serialized
    var start: PackedPos = -1
    @Serialized
    var destination: PackedPos = -1
    /**
     * The routine will be calculated locally by [start] and [destination]
     */
    var routine: Path? = null
    /**
     * It indicates a payload to be sent in [PayloadDataList] in current node.
     */
    @Serialized
    var curData: Payload? = null
    val isActive: Boolean
        get() = start != -1 || destination != -1

    fun finish() {
        start = -1
        destination = -1
        curData = null
        routine?.release()
        routine = null
    }
}

class Path : LinkedPath<INetworkNode>(), Pool.Poolable {
    var ownerCounter = 0
    fun tryDeconstruct() {
        if (ownerCounter == 0) {
            DataNetwork.pathPool.free(this)
        } else if (ownerCounter < 0) {
            CLog.warn("Path(${path.joinToString(",")}) has negative owner counter.")
        }
    }
    @Suppress("UNUSED_PARAMETER")
    fun bind(any: Any) {
        ownerCounter++
    }

    fun release() {
        ownerCounter--
    }
    @Deprecated(
        "This is called by object pool, don't use this",
        ReplaceWith("path.tryDeconstruct()"),
        level = DeprecationLevel.ERROR
    )
    override fun reset() {
        path.clear()
    }
}

fun Path.reversedPath(): Path {
    val reversed = DataNetwork.pathPool.obtain()
    reversed.path.addAll(this.path)
    reversed.path.reverse()
    return reversed
}

class Pointer : BFS.IPointer<INetworkNode>, Pool.Poolable {
    override var previous: BFS.IPointer<INetworkNode>? = null
    override var self: INetworkNode = EmptyNetworkNode
    override fun reset() {
        previous = null
        self = EmptyNetworkNode
    }
}
