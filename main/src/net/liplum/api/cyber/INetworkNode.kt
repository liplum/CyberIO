package net.liplum.api.cyber

import mindustry.Vars
import net.liplum.api.ICyberEntity
import net.liplum.data.DataNetwork
import net.liplum.data.PayloadData
import net.liplum.lib.Out
import net.liplum.lib.Serialized
import net.liplum.lib.utils.forEach
import net.liplum.mdt.utils.Pos
import plumy.pathkt.IVertex

interface INetworkNode : ICyberEntity, IVertex<INetworkNode> {
    @Serialized
    var dataMod: NetworkModule
    var networkGraph: DataNetwork
        get() = dataMod.network
        set(value) {
            dataMod.network = value
        }
    @Serialized
    val data: PayloadData
    @Serialized
    val currentOriented: Pos
    /**
     * [0f,1f]
     */
    @Serialized
    val sendingProgress: Float
    var routine: DataNetwork.Path?

    companion object {
        private val tempList = ArrayList<INetworkNode>()
    }

    fun canTransferTo(other: INetworkNode): Boolean =
        routine != null && routine == other.routine

    override val linkedVertices: Iterable<INetworkNode>
        get() = getNetworkConnections(tempList)

    fun link(target: INetworkNode) {
        dataMod.links.addUnique(target.building.pos())
        target.dataMod.links.addUnique(this.building.pos())
        this.dataMod.network.merge(target)
    }

    fun unlink(target: INetworkNode) {
        dataMod.links.removeValue(target.building.pos())
        target.dataMod.links.removeValue(this.building.pos())
        val selfNewGraph = DataNetwork()
        selfNewGraph.reflow(this)
        val targetNewGraph = DataNetwork()
        targetNewGraph.reflow(target)
    }
    /**
     * @return [out]
     */
    fun getNetworkConnections(@Out out: MutableList<INetworkNode>): MutableList<INetworkNode> {
        out.clear()
        dataMod.links.forEach {
            val link = Vars.world.tile(it)
            val b = link?.build
            if (b != null && b.team == building.team) {
                val node = b.nn()
                if (node != null)
                    out.add(node)
            }
        }
        return out
    }
}