package net.liplum.blocks.cloud

import arc.struct.OrderedSet
import net.liplum.animations.blocks.BlockGroupObj

class CloudInfo {
    var sendersPos = OrderedSet<Int>()
    lateinit var aniBlockGroupObj: BlockGroupObj<Cloud, Cloud.CloudBuild>
    var lastReceiveOrSendDataTime = 31f
    var lastShredTime = 61f
    val isDataTransferring: Boolean
        get() = lastReceiveOrSendDataTime <= 30f
    val isShredding: Boolean
        get() = lastShredTime <= 60f
}