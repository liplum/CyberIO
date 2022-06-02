package net.liplum.api.cyber

import mindustry.Vars
import mindustry.world.Block
import net.liplum.api.ICyberEntity
import net.liplum.api.cyber.SideLinks.Companion.reflect
import net.liplum.data.DataNetwork
import net.liplum.data.PayloadData
import net.liplum.lib.Out
import net.liplum.lib.Serialized
import net.liplum.lib.utils.snapshotForEach
import net.liplum.mdt.CalledBySync
import net.liplum.mdt.SendDataPack
import net.liplum.mdt.utils.*
import plumy.pathkt.IVertex

interface INetworkNode : ICyberEntity, IVertex<INetworkNode> {
    @Serialized
    var network: DataNetwork
    var init: Boolean
    var links: SideLinks
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
        private val tempSideLinksBytes = ByteArray(SideLinks.dataSize)
        private val linksForSync = SideLinks()
    }

    fun canTransferTo(other: INetworkNode): Boolean =
        routine != null && routine == other.routine

    override val linkedVertices: Iterable<INetworkNode>
        get() = getNetworkConnections(tempList)
    /**
     * The [copy] is only used for sync, any change should be based on it.
     * @see [receiveSideLinksBytes]
     */
    @SendDataPack
    fun sendSideLinks(copy: SideLinks) {
        copy.write(tempSideLinksBytes)
        sendSideLinksBytes(tempSideLinksBytes)
    }
    /**
     * @see [receiveSideLinksBytes]
     */
    @SendDataPack
    fun sendSideLinksBytes(bytes: ByteArray)
    /**
     * Receive the side links.
     * It will deal with the graph merging and link each other
     */
    @CalledBySync
    fun receiveSideLinksBytes(bytes: ByteArray) {
        links.readAndJustify(bytes, onAdded = {added ->
            added.TEAny<INetworkNode>()?.let {
                it
                this.network.merge(it)
            }
        }, onRemoved = { removed ->
            removed.TEAny<INetworkNode>()?.let {
                network.reflow(this)
                val targetNewGraph = DataNetwork()
                targetNewGraph.reflow(it)
            }
        })
    }

    fun isLinkedWith(side: Side, other: INetworkNode) =
        other.building.pos() == links[side]

    fun isLinkedWith(other: INetworkNode) =
        other.building.pos() in links
    /**
     * Link to [target] relative to self's [side].
     * From [target] side, it's the reflection.
     * This doesn't take whether the side is empty into account.
     * It should be considered before calling this.
     * It will sync the side links.
     */
    @SendDataPack
    fun linkSync(side: Side, target: INetworkNode) {
        linksForSync.copyFrom(links)
        linksForSync[side] = target
        links.whenDirty {
            sendSideLinks(linksForSync)
        }
        // don't need to change [target]'s links, sync will solve this.
        // target.links[side.reflect] = this
    }
    /**
     * Unlink to [target] relative to self's [side].
     * From [target] side, it's the reflection.
     * This doesn't take whether the side is empty into account.
     * It should be considered before calling this.
     * It will sync the side links.
     */
    @SendDataPack
    fun unlinkSync(side: Side, target: INetworkNode) {
        linksForSync.copyFrom(links)
        linksForSync[side] = -1
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
    fun getNetworkConnections(@Out out: MutableList<INetworkNode>):
            MutableList<INetworkNode> {
        out.clear()
        links.forEachNode { out.add(it) }
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
        links.forEachNode {
            unlink(it)
        }
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
            b.building.configure()
        }
        block.configClear { b: INetworkNode ->
            b.clearLinks()
        }
    }
}