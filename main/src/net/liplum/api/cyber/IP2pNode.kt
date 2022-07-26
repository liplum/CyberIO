package net.liplum.api.cyber

import mindustry.type.Liquid
import net.liplum.api.ICyberEntity
import net.liplum.mdt.SendDataPack

interface IP2pNode : ICyberEntity {
    val currentFluid: Liquid
    val currentAmount: Float
    var connected: IP2pNode?
        @SendDataPack set

    fun isConnectedTo(other: IP2pNode) = other == connected
    fun streamToAnother(amount: Float)
    fun readSteam(fluid: Liquid, amount: Float)

    companion object {
        @SendDataPack
        fun IP2pNode.connectToSync(other: Int) {
            other.p2p()?.let { connected = it }
        }
    }
}