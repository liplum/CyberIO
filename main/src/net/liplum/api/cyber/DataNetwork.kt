package net.liplum.api.cyber

import arc.struct.IntMap
import arc.struct.IntSet
import arc.struct.Seq
import arc.util.pooling.Pool
import arc.util.pooling.Pools
import mindustry.game.EventType
import mindustry.gen.Building
import net.liplum.annotations.SubscribeEvent
import plumy.core.arc.forLoop
import plumy.core.arc.set
import net.liplum.data.DataID
import net.liplum.data.EmptyDataID
import net.liplum.data.PayloadData
import net.liplum.data.PayloadDataList
import plumy.core.Serialized
import net.liplum.common.delegate.Delegate
import net.liplum.common.util.Index
import plumy.core.ClientOnly
import plumy.pathkt.*
import java.util.*

class DataNetwork {
    val entity = DataNetworkUpdater.create().apply {
        network = this@DataNetwork
    }
    val nodes = Seq<INetworkNode>()
    val routineCache by lazy { HashMap<Any, Path>() }
    var id = lastNetworkID++
        private set
    val size: Int
        get() = nodes.size
    val dataId2Task by lazy { IntMap<TransferTask>() }
    @ClientOnly
    val onDataInventoryChangedEvent = Delegate()
    fun update() {
        if (nodes.size <= 0) return // if no node here, do nothing
        for (node in nodes) {
            val request = node.request
            if (request == EmptyDataID) continue
            val task: TransferTask? = dataId2Task[request]
            if (task == null) {
                val holder = findWhoHasDataByID(request)
                if (holder != null) {
                    addNewTask(node, holder, request)
                }
            }
        }
        if (dataId2Task.size <= 0) return
        val tasks = dataId2Task.values()
        while (tasks.hasNext()) {
            val task = tasks.next()
            val progress = task.curProgress
            val path = task.routine ?: task.run {
                reload()
                routine
            } ?: continue // If the path still doesn't exist, skip it.
            if (task.validate(path, progress)) {
                if (progress == path.size - 1) {
                    // the data reached the destination
                    task.free()
                    tasks.remove()
                } else {
                    // the data doesn't reach the destination, including just one node remaining
                    val curNode = path[progress]
                    val nextNode = path[progress + 1]
                    curNode.setOriented(nextNode)
                    curNode.dataInSending = task.request
                }
            } else { // if the progress is not accurate
                val curProgress = path.refindNode {
                    it.hasData(task.request)
                }
                if (curProgress < 0) {
                    // I don't know what happened, but the data may have reached the destination or disappear.
                    task.free()
                    tasks.remove()
                } else {
                    task.curProgress = curProgress
                }
            }
        }
    }

    fun onDataInventoryChanged() {
        onDataInventoryChangedEvent()
    }

    fun onNetworkNodeChanged() {
        clearRoutineCache()
        onDataInventoryChanged()
    }

    fun advanceProgress(requestID: DataID) {
        dataId2Task[requestID]?.let {
            it.curProgress++
            if (it.curProgress >= (it.routine?.size ?: 0)) {
                it.free()
                dataId2Task.remove(it.request)
            }
        }
    }

    private fun TransferTask.validate(path: Path, progress: Index): Boolean {
        if (progress in 0 until path.size) {
            return path[progress].hasData(request)
        }
        return false
    }

    private inline fun Path.refindNode(predicate: (INetworkNode) -> Boolean): Index =
        path.indexOfFirst(predicate)

    private fun addNewTask(
        applicant: INetworkNode,
        holder: INetworkNode,
        request: DataID,
    ) {
        val task = taskPool.obtain().apply {
            start = holder
            destination = applicant
            this.request = request
            routine = generatePath(holder, applicant)
            if (routine != null)
                curProgress = 0
        }
        dataId2Task[request] = task
    }
    /**
     * It's useful when the task is synchronized from remote.
     * It will re-generate the path if needed.
     */
    private fun TransferTask.reload() {
        val destination = destination
        val start = start
        if (routine == null && start != null && destination != null) {
            routine = generatePath(start, destination)
        }
    }
    /**
     * Generate a path and cache it.
     */
    private fun generatePath(start: INetworkNode, destination: INetworkNode): Path? {
        val toKey = genStart2DestinationKey(start, destination)
        var toPath = routineCache[toKey]
        if (toPath != null) {
            return toPath
        } else {
            val fromPath = routineCache[destination, start]
            return if (fromPath != null) {
                toPath = fromPath.reversedPath()
                routineCache[toKey] = toPath
                toPath
            } else {
                // Or using the shortest path?
                val path = findPath(start, destination)
                if (path != null) {
                    routineCache[toKey] = path
                    path
                } else null
            }
        }
    }

    private operator fun HashMap<Any, Path>.set(
        start: INetworkNode,
        destination: INetworkNode,
        path: Path,
    ) {
        this[genStart2DestinationKey(start, destination)] = path
    }

    private operator fun HashMap<Any, Path>.get(
        start: INetworkNode,
        destination: INetworkNode,
    ): Path? = this[genStart2DestinationKey(start, destination)]

    private fun findWhoHasDataByID(id: DataID): INetworkNode? {
        nodes.forLoop {
            if (it.hasData(id)) return it
        }
        return null
    }
    /**
     * Find a path from [start] to [destination].
     * Caller should ensure the path exists.
     */
    fun findPath(start: INetworkNode, destination: INetworkNode): Path? {
        val path = bfsContainer.findPathBFS(start, destination)
        return if (path.isEmpty()) {
            path.free()
            null
        } else {
            path.reverse()// It uses [ReversedArrayPath]
            path
        }
    }

    fun remove() {
        entity.remove()
        nodes.clear()
        clearRoutineCache()
    }

    private fun clearRoutineCache() {
        routineCache.forEach { (_, path) ->
            path.free()
        }
        routineCache.clear()
    }
    @Deprecated("Unused", ReplaceWith("DataNetwork.merge"))
    fun mergeNetwork(other: DataNetwork) {
        if (other == this) return
        other.entity.remove()
        other.nodes.forEach(::add)
    }
    /**
     * Initialize a node, used in [Building.create]
     */
    fun initNode(node: INetworkNode) {
        add(node)
    }
    /**
     * Add a node into this network and initialize it.
     */
    private fun add(node: INetworkNode) {
        if (node.network != this || !node.init) {
            node.network = this
            node.init = true
            nodes.add(node)
            entity.add()
            onNetworkNodeChanged()
        }
    }
    /**
     * Merge a node into this network
     */
    private fun merge(node: INetworkNode) {
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

    inline fun forEachDataIndexed(func: (Int, INetworkNode, PayloadData) -> Unit) {
        var i = 0
        for (node in nodes) {
            for (data in node.dataList) {
                func(i, node, data)
                i++
            }
        }
    }

    inline fun forEachData(func: (INetworkNode, PayloadData) -> Unit) {
        for (node in nodes) {
            for (data in node.dataList)
                func(node, data)
        }
    }

    override fun toString() =
        "DataNetwork#$id"

    companion object {
        val taskPool: Pool<TransferTask> = Pools.get(TransferTask::class.java, ::TransferTask)
        /**
         * It will
         */
        val pathPool: Pool<Path> = Pools.get(Path::class.java, ::Path)
        @Suppress("MoveLambdaOutsideParentheses")
        private val bfsContainer = EasyContainer<INetworkNode, Path>(
            ::Pointer,
            { pathPool.obtain() },
        )
        private val tmp1 = ArrayList<INetworkNode>()
        private val bfsQueue = LinkedList<INetworkNode>()
        private val closedSet = IntSet()
        @JvmStatic
        private var lastNetworkID = 0
        private fun genStart2DestinationKey(start: INetworkNode, dest: INetworkNode): Any =
            start.building.id * 31 + dest.building.id

        var curPayloadDataID = 0
        fun assignDataID(): Int =
            curPayloadDataID++
        @SubscribeEvent(EventType.WorldLoadEvent::class)
        fun resetPayloadDataID() {
            curPayloadDataID = 0
        }

        fun mergeToLagerNetwork(a: INetworkNode, b: INetworkNode) {
            if (a.network.size >= b.network.size) {
                a.network.merge(b)
            } else {
                b.network.merge(a)
            }
        }
    }
}

class TransferTask : Pool.Poolable {
    @Serialized
    var start: INetworkNode? = null
    @Serialized
    var destination: INetworkNode? = null
    /**
     * The routine will be calculated locally by [start] and [destination]
     */
    var routine: Path? = null
    /**
     * It indicates a payload to be sent in [PayloadDataList] in current node.
     */
    @Serialized
    var request: DataID = EmptyDataID
    @Serialized
    var curProgress: Index = -1
    fun free() {
        DataNetwork.taskPool.free(this)
    }

    override fun reset() {
        start = null
        destination = null
        request = EmptyDataID
        // Mustn't call `routine?.free()`, the path is cached somewhere
        routine = null
    }
}

class Path internal constructor() : ReversedArrayPath<INetworkNode>(), Pool.Poolable {
    fun free() {
        DataNetwork.pathPool.free(this)
    }

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
/**
 * Pointer object is small, there is no need to pool it.
 */
class Pointer internal constructor() : IPointer<INetworkNode> {
    override var previous: IPointer<INetworkNode>? = null
    override var self: INetworkNode = EmptyNetworkNode
}
