package net.liplum.api.data

import mindustry.world.Block
import net.liplum.R
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

fun Block.drawLinkedLineToReceiverWhenConfiguring(x: Int, y: Int) {
    CyberU.drawLinkedLineToReceiverWhenConfiguring(this, x, y)
}

inline fun whenNotConfiguringSender(func: () -> Unit) {
    if (!CyberU.isConfiguringSender()) {
        func()
    }
}