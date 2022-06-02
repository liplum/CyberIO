package net.liplum.api.cyber

import mindustry.Vars
import mindustry.gen.Building
import mindustry.world.Block
import net.liplum.api.ICyberEntity
import net.liplum.data.DataNetwork
import net.liplum.data.PayloadData
import net.liplum.lib.Out
import net.liplum.lib.Serialized
import net.liplum.lib.utils.forEach
import net.liplum.lib.utils.snapshotForEach
import net.liplum.mdt.CalledBySync
import net.liplum.mdt.SendDataPack
import net.liplum.mdt.utils.*
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

    fun isLinkedWith(other: INetworkNode) =
        other.building.pos() in dataMod.links
    /**
     * Using [Building.configure] (packedPos:Int) as default
     */
    @SendDataPack
    fun linkSync(target: INetworkNode) {
        building.configure(target.building.pos())
    }

    fun link(target: INetworkNode) {
        dataMod.links.addUnique(target.building.pos())
        target.dataMod.links.addUnique(this.building.pos())
        this.dataMod.network.merge(target)
    }

    fun unlink(target: INetworkNode) {
        if (!this.isLinkedWith(target)) return
        dataMod.links.removeValue(target.building.pos())
        target.dataMod.links.removeValue(this.building.pos())
        val selfNewGraph = DataNetwork()
        selfNewGraph.reflow(this)
        val targetNewGraph = DataNetwork()
        targetNewGraph.reflow(target)
    }

    fun onRemovedInWorld() {
        val links = dataMod.links
        if (!links.isEmpty) {
            links.snapshotForEach {
                it.TEAny<INetworkNode>()?.unlink(this)
            }
            links.clear()
        }
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

    val linkRange: WorldXY
    val tileLinkRange: TileXY
        get() = (linkRange / Vars.tilesize).toInt()
    val maxLink: Int
    fun canLink(target: INetworkNode): Boolean {
        if (!canLinkMore ||
            !target.canLinkMore ||
            target.building.pos() in dataMod.links ||
            building.pos() in target.dataMod.links
        ) return false
        return target.building.dst(building) <= linkRange
    }

    val canLinkMore: Boolean
        get() = dataMod.links.size < maxLink
    @CalledBySync
    fun linkNodeFromRemote(pos: PackedPos) {
        val target = pos.nn() ?: return
        val contains = dataMod.links.contains(pos)
        if (contains) {
            unlink(target)
        } else if (canLink(target)) { // Try to link to it
            link(target)
        }
    }
    @CalledBySync
    fun clearLinks() {
        dataMod.links.clear()
    }
}

interface INetworkBlock {
    val linkRange: WorldXY
    val maxLink: Int
    val tileLinkRange: TileXY
        get() = (linkRange / Vars.tilesize).toInt()
    val block: Block
    val expendPlacingLineTime: Float
    fun initDataNetworkRemoteConfig() {
        block.config(Integer::class.java) { b: INetworkNode, i ->
            b.linkNodeFromRemote(i.toInt())
        }
        block.configClear { b: INetworkNode ->
            b.clearLinks()
        }
    }
}