package net.liplum.api.cyber

import mindustry.type.Liquid
import net.liplum.api.ICyberEntity
import net.liplum.mdt.SendDataPack
import net.liplum.mdt.utils.PackedPos

interface IP2pNode : ICyberEntity {
    val currentFluid: Liquid
    val currentAmount: Float
    val maxRange: Float
    var connectedPos: PackedPos
    val connected: IP2pNode?
        get() = connectedPos.p2p()
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