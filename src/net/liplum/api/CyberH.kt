package net.liplum.api

import mindustry.world.Block
import net.liplum.R
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender
import net.liplum.api.stream.IStreamClient
import net.liplum.api.stream.IStreamHost
import net.liplum.utils.G

fun IDataSender.drawDataNetGraphic() {
    G.drawSurroundingCircle(tile, R.C.Sender)
    if (canMultipleConnect()) {
        CyberU.drawReceivers(this, connectedReceivers())
    } else {
        CyberU.drawReceiver(this, connectedReceiver())
    }
}

fun IDataReceiver.drawDataNetGraphic() {
    G.drawSurroundingCircle(tile, R.C.Receiver)
    CyberU.drawSenders(this, connectedSenders())
}

fun IDataReceiver.drawRequirements() {
    CyberU.drawRequirements(this)
}
fun Block.drawLinkedLineToReceiverWhenConfiguring(x: Int, y: Int) {
    CyberU.drawLinkedLineToReceiverWhenConfiguring(this, x, y)
}

inline fun whenNotConfiguringSender(func: () -> Unit) {
    if (!CyberU.isConfiguringSender()) {
        func()
    }
}

fun IStreamHost.drawStreamGraphic() {
    G.drawSurroundingCircle(tile, hostColor)
    CyberU.drawClients(this, connectedClients())
}

fun IStreamClient.drawStreamGraphic() {
    G.drawSurroundingCircle(tile, clientColor)
    CyberU.drawHosts(this, connectedHosts())
}

fun IStreamClient.drawRequirements() {
    CyberU.drawRequirements(this)
}

fun Block.drawLinkedLineToClientWhenConfiguring(x: Int, y: Int) {
    CyberU.drawLinkedLineToClientWhenConfiguring(this, x, y)
}

inline fun whenNotConfiguringHost(func: () -> Unit) {
    if (!CyberU.isConfiguringHost()) {
        func()
    }
}