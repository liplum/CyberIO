package net.liplum.api.cyber

import arc.math.geom.Geometry
import arc.math.geom.Point2
import net.liplum.common.util.copyFrom
import plumy.dsl.PackedPos
import plumy.dsl.castBuild

typealias Side = Int

const val RIGHT = 0
const val TOP = 1
const val LEFT = 2
const val BOTTOM = 3
/**
 * SideLinks doesn't need sync. It should be updated every tick.
 */
class SideLinks {
    /**
     * ```
     *          ▲
     *         1│y
     *          │
     *  2       │       0
     * ─────────┼─────────►
     *          │         x
     *          │
     *         3│
     * ```
     */
    var links = IntArray(4) { -1 }
    fun isEmpty(side: Side) = links[side] == -1
    var right: PackedPos
        get() = links[RIGHT]
        set(value) {
            links[RIGHT] = value
        }
    var top: PackedPos
        get() = links[TOP]
        set(value) {
            links[TOP] = value
        }
    var left: PackedPos
        get() = links[LEFT]
        set(value) {
            links[LEFT] = value
        }
    var bottom: PackedPos
        get() = links[BOTTOM]
        set(value) {
            links[BOTTOM] = value
        }
    var rightNode: INetworkNode?
        get() = links[0].castBuild()
        set(value) {
            right = value?.building?.pos() ?: -1
        }
    var topNode: INetworkNode?
        get() = links[1].castBuild()
        set(value) {
            top = value?.building?.pos() ?: -1
        }
    var leftNode: INetworkNode?
        get() = links[2].castBuild()
        set(value) {
            left = value?.building?.pos() ?: -1
        }
    var bottomNode: INetworkNode?
        get() = links[3].castBuild()
        set(value) {
            bottom = value?.building?.pos() ?: -1
        }

    operator fun get(side: Side): PackedPos =
        links[side]

    operator fun set(side: Side, pos: PackedPos) {
        links[side] = pos
    }

    operator fun set(side: Side, node: INetworkNode) {
        val pos = node.building.pos()
        links[side] = pos
    }

    operator fun contains(pos: PackedPos): Boolean {
        for (side in RIGHT..BOTTOM) {
            links[side] = pos
        }
        return false
    }

    fun count(): Int {
        var counter = 0
        for (side in RIGHT..BOTTOM) {
            if (links[side] != -1)
                counter++
        }
        return counter
    }
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
    /**
     * Only iterate [Side] on the non-empty sides.
     * In order of [RIGHT] to [BOTTOM]
     */
    inline fun forEachSide(cons: (Side) -> Unit) {
        for (side in RIGHT..BOTTOM) {
            if (links[side] != -1)
                cons(side)
        }
    }
    /**
     * Only iterate [Side] on any side no matter whether it's empty.
     * In order of [RIGHT] to [BOTTOM]
     */
    inline fun forEach(cons: (Side) -> Unit) {
        for (side in RIGHT..BOTTOM) {
            cons(side)
        }
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
            val node = links[side].castBuild<INetworkNode>()
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
            val node = links[side].castBuild<INetworkNode>()
            if (node != null)
                cons(side, node)
        }
    }
    /**
     * Only iterate [PackedPos] on the non-empty sides.
     * In order of [RIGHT] to [BOTTOM]
     */
    inline fun forEachUnlinkSide(cons: (Side) -> Unit) {
        for (side in RIGHT..BOTTOM) {
            if (links[side] == -1)
                cons(side)
        }
    }

    override fun toString() = links.toString()

    companion object {
        /**
         * Get the reflected side index in [Geometry.d4]
         */
        val Side.reflect: Side
            get() = (this + 2) % 4
        val coordinates: Array<Point2> = Geometry.d4.clone()
        val enableAllSides = SideEnable(4) { true }
    }
}
typealias SideEnable = BooleanArray

