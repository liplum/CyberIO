package net.liplum.api.cyber

import arc.graphics.Color
import mindustry.type.Liquid
import net.liplum.api.ICyberEntity
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.SendDataPack
import net.liplum.mdt.utils.PackedPos

enum class P2pStatus {
    None, Sender, Receiver
}

interface IP2pNode : ICyberEntity {
    val currentFluid: Liquid
    val currentAmount: Float
    val maxRange: Float
    var connectedPos: PackedPos
    /**
     * Who should draw the link
     */
    @ClientOnly
    var isDrawer: Boolean
    @ClientOnly
    var status: P2pStatus
    @ClientOnly
    val color: Color
        get() = currentFluid.color
    val connected: IP2pNode?
        get() = connectedPos.p2p()
    val isConnected: Boolean
        get() = connected != null
    @SendDataPack
    fun disconnectFromAnotherSync()
    @SendDataPack
    fun connectToSync(other: IP2pNode)
    fun isConnectedTo(other: IP2pNode) = other == connected
    fun streamToAnother(amount: Float)
    fun readSteam(fluid: Liquid, amount: Float)
    val restRoom: Float
        get() = (block.liquidCapacity - currentAmount).coerceAtLeast(0f)

    companion object {
        @SendDataPack
        fun IP2pNode.connectToSync(other: Int) {
            other.p2p()?.let { connectToSync(it) }
        }
    }
}