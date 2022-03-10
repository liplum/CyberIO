package net.liplum.blocks.gadgets

import arc.math.Mathf
import arc.struct.OrderedSet
import mindustry.type.Item
import mindustry.world.Tile
import net.liplum.ClientOnly
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.config
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender
import net.liplum.blocks.AniedBlock

private typealias AniStateD = AniState<SmartDistributor, SmartDistributor.SmartDISBuild>

open class SmartDistributor(name: String) : AniedBlock<SmartDistributor, SmartDistributor.SmartDISBuild>(name) {
    @ClientOnly lateinit var RequireAni: AniStateD
    @ClientOnly lateinit var NoPowerAni: AniStateD

    init {
        solid = true
        update = true
        hasItems = true
    }

    open inner class SmartDISBuild : AniedBlock<SmartDistributor, SmartDistributor.SmartDISBuild>.AniedBuild(),
        IDataReceiver {
        var senders = OrderedSet<Int>()
        override fun acceptData(sender: IDataSender, item: Item?): Boolean {
            TODO("Not yet implemented")
        }

        override fun receiveData(sender: IDataSender, item: Item?, amount: Int) {
            TODO("Not yet implemented")
        }

        override fun canAcceptAnyData(sender: IDataSender): Boolean {
            TODO("Not yet implemented")
        }

        override fun isOutputting() = true
        override fun connect(sender: IDataSender) {
            senders.add(sender.building.pos())
        }

        override fun disconnect(sender: IDataSender) {
            senders.remove(sender.building.pos())
        }

        override fun connectedSenders() = senders
        override fun connectedSender(): Int? = senders.first()
        override fun acceptConnection(sender: IDataSender): Boolean {
            TODO("Not yet implemented")
        }

        override fun getBuilding() = this
        override fun getTile(): Tile = this.tile
        override fun getBlock() = this@SmartDistributor
    }

    override fun genAniConfig() {
        config {
            From(RequireAni) To NoPowerAni When { _, build ->
                Mathf.zero(build.power.status)
            }
            From(NoPowerAni) To RequireAni When { _, build ->
                !Mathf.zero(build.power.status)
            }
        }
    }

    override fun genAniState() {
        RequireAni = addAniState("Require")
        NoPowerAni = addAniState("NoPower")
    }
}