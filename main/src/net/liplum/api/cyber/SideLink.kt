package net.liplum.api.cyber

import arc.math.geom.Geometry
import arc.struct.IntSeq
import net.liplum.lib.utils.copyFrom
import net.liplum.lib.utils.forEach
import net.liplum.lib.utils.readInt
import net.liplum.lib.utils.writeInt
import net.liplum.mdt.utils.PackedPos
import net.liplum.mdt.utils.TEAny

typealias Side = Int

const val RIGHT = 0
const val TOP = 1
const val LEFT = 2
const val BOTTOM = 3

class SideLinks {
    /**
     * ```
     *          ▲
     *         1│y
     *          │
     *  2       │       0
     * ─────────┼──────────►
     *          │          x
     *          │
     *         3│
     * ```
     */
    var links = IntArray(4) { -1 }
    var dirty = false
    fun isEmpty(side: Side) = links[side] == -1
    /** Mark dirty */
    var right: PackedPos
        get() = links[RIGHT]
        set(value) {
            if (links[RIGHT] != value) {
                links[RIGHT] = value
                dirty = true
            }
        }
    /** Mark dirty */
    var top: PackedPos
        get() = links[TOP]
        set(value) {
            if (links[TOP] != value) {
                links[TOP] = value
                dirty = true
            }
        }
    /** Mark dirty */
    var left: PackedPos
        get() = links[LEFT]
        set(value) {
            if (links[LEFT] != value) {
                links[LEFT] = value
                dirty = true
            }
        }
    /** Mark dirty */
    var bottom: PackedPos
        get() = links[BOTTOM]
        set(value) {
            if (links[BOTTOM] != value) {
                links[BOTTOM] = value
                dirty = true
            }
        }
    /** Mark dirty */
    var rightNode: INetworkNode?
        get() = links[0].TEAny()
        set(value) {
            right = value?.building?.pos() ?: -1
        }
    /** Mark dirty */
    var topNode: INetworkNode?
        get() = links[1].TEAny()
        set(value) {
            top = value?.building?.pos() ?: -1
        }
    /** Mark dirty */
    var leftNode: INetworkNode?
        get() = links[2].TEAny()
        set(value) {
            left = value?.building?.pos() ?: -1
        }
    /** Mark dirty */
    var bottomNode: INetworkNode?
        get() = links[3].TEAny()
        set(value) {
            bottom = value?.building?.pos() ?: -1
        }

    operator fun get(side: Side): PackedPos =
        links[side]
    /**
     * Mark dirty
     */
    operator fun set(side: Side, pos: PackedPos) {
        if (links[side] != pos) {
            links[side] = pos
            dirty = true
        }
    }
    /**
     * Mark dirty
     */
    operator fun set(side: Side, node: INetworkNode) {
        val pos = node.building.pos()
        if (links[side] != pos) {
            links[side] = pos
            dirty = true
        }
    }

    operator fun contains(pos: PackedPos) =
        right == pos || top == pos || left == pos || bottom == pos
    /**
     * Copy [other] into this. It marks dirty if necessary
     * @return self
     */
    fun copyFrom(other: SideLinks): SideLinks {
        right = other.right
        top = other.top
        left = other.left
        bottom = other.bottom
        return this
    }
    /**
     * Copy [other] into this. Just copy the low-level array without dirty
     * @return self
     */
    fun plainCopyFrom(other: SideLinks): SideLinks {
        links.copyFrom(other.links)
        return this
    }

    fun write(bytes: ByteArray) {
        bytes.apply {
            var offset = 0
            for (side in RIGHT..BOTTOM) {
                offset = writeInt(links[side], offset)
            }
        }
    }
    /**
     * Read and mark dirty if necessary
     */
    fun read(bytes: ByteArray) {
        bytes.apply {
            var offset = 0
            for (side in RIGHT..BOTTOM) {
                offset = readInt(offset) {
                    this@SideLinks[side] = it
                }
            }
        }
    }
    /**
     * @param onAdded (old pos, new pos) -> Unit
     */
    inline fun readAndJustify(
        bytes: ByteArray,
        onAdded: (PackedPos) -> Unit,
        onRemoved: (PackedPos) -> Unit
    ) {
        val added = tempIntSeq1
        added.clear()
        val removed = tempIntSeq2
        removed.clear()
        bytes.apply {
            var offset = 0
            for (side in RIGHT..BOTTOM) {
                offset = readInt(offset) { new->
                    val old = links[side]
                    if (new == -1) {
                        if (old != -1) {
                            // When old is a node while new is empty
                            links[side] = -1
                            removed.add(old)
                        }
                    } else {// new is a node
                        links[side] = new
                        added.add(new)
                        if(old != -1)
                            removed.add(old)
                    }
                }
            }
        }
        added.forEach { onAdded(it) }
        removed.forEach { onRemoved(it) }
    }
    /**
     * Find the side of this node.
     * @return the side of [target] or -1 if not found
     */
    fun findSide(target: INetworkNode): Side {
        forEachNodeWithSide { side, node ->
            if (target == node) return side
        }
        return -1
    }

    fun clear(side: Side) {
        links[side]
        dirty = true
    }
    /**
     * Only iterate [PackedPos] on the non-empty sides.
     * In order of [RIGHT] to [BOTTOM]
     */
    inline fun forEachPos(cons: (PackedPos) -> Unit) {
        for (side in RIGHT..BOTTOM) {
            if (links[side] != -1)
                cons(links[side])
        }
    }
    /**
     * Only iterate [PackedPos] on the non-empty sides.
     * In order of [RIGHT] to [BOTTOM]
     */
    inline fun forEachPosWithSide(cons: (Side, PackedPos) -> Unit) {
        for (side in RIGHT..BOTTOM) {
            if (links[side] != -1)
                cons(side, links[side])
        }
    }
    /**
     * Only iterate [INetworkNode] on the non-empty sides.
     * In order of [RIGHT] to [BOTTOM]
     */
    inline fun forEachNode(cons: (INetworkNode) -> Unit) {
        for (side in RIGHT..BOTTOM) {
            val node = links[side].TEAny<INetworkNode>()
            if (node != null)
                cons(node)
        }
    }
    /**
     * Only iterate [PackedPos] on the non-empty sides.
     * In order of [RIGHT] to [BOTTOM]
     */
    inline fun forEachNodeWithSide(cons: (Side, INetworkNode) -> Unit) {
        for (side in RIGHT..BOTTOM) {
            val node = links[side].TEAny<INetworkNode>()
            if (node != null)
                cons(side, node)
        }
    }
    /**
     * Execute an action when this is dirty.
     * Then reset the dirty mark.
     * It's useful for data synchronization.
     */
    inline fun whenDirty(func: () -> Unit) {
        if (dirty) func()
        dirty = false
    }

    companion object {
        val tempIntSeq1 = IntSeq(16)
        val tempIntSeq2 = IntSeq(16)
        const val dataSize = Int.SIZE_BYTES * 4
        /**
         * Get the reflected side index in [Geometry.d4]
         */
        val Side.reflect: Side
            get() = (this + 2) % 4
    }
}

