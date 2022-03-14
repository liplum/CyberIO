package net.liplum.api.data

import net.liplum.R
import net.liplum.utils.G

fun IDataSender.drawDataNetGraphic() {
    G.init()
    G.drawSurroundingCircle(tile, R.C.Sender)
    if (canMultipleConnect()) {
        CyberU.drawReceivers(this, connectedReceivers())
    } else {
        CyberU.drawReceiver(this, connectedReceiver())
    }
}

fun IDataReceiver.drawDataNetGraphic() {
    G.init()
    G.drawSurroundingCircle(tile, R.C.Receiver)
    CyberU.drawSenders(this, connectedSenders())
}