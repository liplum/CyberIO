package net.liplum.api.cyber

import net.liplum.mdt.utils.PackedPos
import net.liplum.mdt.utils.TEAny

/**
 * A kind of [INetworkNode] but can only link with others in Cardinal directions
 */
interface ISideNetworkNode : INetworkNode {
    val sideLinks: IntArray
    fun unlinkSide(side: Int) {
        val pos: PackedPos = sideLinks[side.coerceIn(0, 3)]
        val node = pos.TEAny<INetworkNode>()
        if (node != null) {
            if (node is ISideNetworkNode) {
            }
            sideLinks[side] = -1
            pos.TEAny<INetworkNode>()?.let {
                unlink(it)
            }
        }
    }

    fun linkSide(side: Int, target: INetworkNode) {
        sideLinks[side.coerceIn(0, 3)] = target.building.pos()
        link(target)
    }

    fun isSideLinkedWith(other: ISideNetworkNode, side: Int): Boolean {
        return sideLinks[side].TEAny<ISideNetworkNode>() == other ||
                super.isLinkedWith(other)
    }

    fun canSideLink(target: ISideNetworkNode, side: Int): Boolean {
        return sideLinks[side] < 0 &&
                target.sideLinks[reflect(side)] < 0 &&
                super.canLink(target)
    }
}