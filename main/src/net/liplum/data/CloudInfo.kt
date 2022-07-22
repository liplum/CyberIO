package net.liplum.data

import arc.struct.ObjectSet
import arc.util.Time
import mindustry.Vars
import net.liplum.api.cyber.*
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.animations.blocks.BlockGroupObj

open class CloudInfo(val sharedRoom: SharedRoom) {
    var sendersPos = ObjectSet<Int>()
    var receiversPos = ObjectSet<Int>()
    @ClientOnly lateinit var aniBlockGroupObj: BlockGroupObj<Cloud, Cloud.CloudBuild>
    @JvmField @ClientOnly var lastReceiveOrSendDataTime = 31f
    @JvmField @ClientOnly var lastShredTime = 61f
    @ClientOnly val isDataTransferring: Boolean
        get() = lastReceiveOrSendDataTime <= 30f
    @ClientOnly val isShredding: Boolean
        get() = lastShredTime <= 60f
    var curItemIndex = 0
        set(value) {
            field = value.coerceAtLeast(0) % (Vars.content.items().size - 1)
        }

    open fun checkReceiverPos() {
        receiversPos.removeAll { !it.ds().exists }
    }

    open fun update() {
        if (Time.time % 60f < 1) {
            checkReceiverPos()
        }
        lastReceiveOrSendDataTime += Time.delta
        lastShredTime += Time.delta
        val users = sharedRoom.users
        val itemModule = sharedRoom.sharedItemModule
        val shardBuild = users.first() as IDataSender
        val items = Vars.content.items()
        for (receiverPos in receiversPos) {
            val receiver = receiverPos.dr()
            if (receiver != null) {
                val reqs = receiver.requirements
                if (reqs == null) {
                    var i = curItemIndex
                    while (i < items.size) {
                        val item = items[i]
                        if (itemModule.has(item) &&
                            receiver.acceptedAmount(shardBuild, item).isAccepted()
                        ) {
                            shardBuild.sendData(receiver, item, 1)
                            break
                        }
                        i++
                    }
                    curItemIndex = i
                } else {
                    for (req in reqs) {
                        if (itemModule.has(req) &&
                            receiver.acceptedAmount(shardBuild, req!!).isAccepted()
                        ) {
                            shardBuild.sendData(receiver, req, 1)
                        }
                    }
                }
            }
        }
    }
}