package net.liplum.blocks.stream

import arc.graphics.Color
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.type.Liquid
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.meta.BlockGroup
import net.liplum.CalledBySync
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.SendDataPack
import net.liplum.api.drawStreamGraphic
import net.liplum.api.stream.*
import net.liplum.persistance.intSet
import net.liplum.utils.TR
import net.liplum.utils.addClientInfo
import net.liplum.utils.sub

open class StreamHost(name: String) : Block(name) {
    @JvmField var maxConnection = 5
    @JvmField var liquidColorLerp = 0.5f
    @ClientOnly lateinit var BaseTR: TR
    /**
     * 1 networkSpeed = 60 per seconds
     */
    @JvmField var networkSpeed = 1f
    @JvmField var SharedClientSeq: Seq<IStreamClient> = Seq(
        if (maxConnection == -1) 10 else maxConnection
    )

    init {
        update = true
        solid = true
        configurable = true
        outputsLiquid = false
        group = BlockGroup.liquids
        noUpdateDisabled = true
        hasLiquids = true
        canOverdrive = false
        config(Integer::class.java) { obj: HostBuild, clientPackedPos ->
            obj.setClient(clientPackedPos.toInt())
        }
        configClear<HostBuild> {
            it.clearClients()
        }
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            bars.addClientInfo<HostBuild>()
        }
    }

    open inner class HostBuild : Building(), IStreamHost {
        var clients = OrderedSet<Int>()
        open fun checkClientsPos() {
            clients.removeAll { !it.sc().exists }
        }

        override fun getHostColor(): Color = liquids.current().color
        override fun updateTile() {
            // Check connection every second
            if (Time.time % 60f < 1) {
                checkClientsPos()
            }
            if (!consValid()) return
            SharedClientSeq.clear()
            for (pos in clients) {
                val client = pos.sc()
                if (client != null) {
                    SharedClientSeq.add(client)
                }
            }
            val liquid = liquids.current()
            var needPumped = networkSpeed.coerceAtMost(liquids.currentAmount())
            var per = needPumped / clients.size
            var resetClient = clients.size
            for (client in SharedClientSeq) {
                if (liquid.match(client.requirements)) {
                    val rest = streaming(client, liquid, per)
                    needPumped -= (per - rest)
                }
                resetClient--
                if (resetClient > 0) {
                    per = needPumped / resetClient
                }
            }
            liquids.remove(liquid, networkSpeed - needPumped)
        }

        override fun onProximityAdded() {
            super.onProximityAdded()
            resubscribeRequirementUpdated()
        }

        open fun onClientRequirementsUpdated(client: IStreamClient) {
        }

        open fun onClientsChanged() {
        }

        open fun resubscribeRequirementUpdated() {
            clients.forEach { pos ->
                pos.sc()?.let {
                    it.onRequirementUpdated += ::onClientRequirementsUpdated
                }
            }
        }

        override fun acceptLiquid(source: Building, liquid: Liquid): Boolean {
            return liquids.current() === liquid || liquids.currentAmount() < 0.2f
        }
        @CalledBySync
        open fun setClient(pos: Int) {
            if (pos in clients) {
                pos.sc()?.let {
                    disconnectClient(it)
                    it.disconnect(this)
                }
            } else {
                pos.sc()?.let {
                    connectClient(it)
                    it.connect(this)
                }
            }
        }
        @CalledBySync
        open fun connectClient(client: IStreamClient) {
            if (clients.add(client.building.pos())) {
                onClientsChanged()
                client.onRequirementUpdated += ::onClientRequirementsUpdated
            }
        }
        @CalledBySync
        open fun disconnectClient(client: IStreamClient) {
            if (clients.remove(client.building.pos())) {
                onClientsChanged()
                client.onRequirementUpdated -= ::onClientRequirementsUpdated
            }
        }
        @CalledBySync
        open fun clearClients() {
            clients.forEach { pos ->
                pos.sc()?.let {
                    it.disconnect(this)
                    it.onRequirementUpdated -= ::onClientRequirementsUpdated
                }
            }
            clients.clear()
            onClientsChanged()
        }

        override fun onConfigureTileTapped(other: Building): Boolean {
            if (this === other) {
                deselect()
                configure(null)
                return false
            }
            val pos = other.pos()
            if (pos in clients) {
                if (maxConnection == 1) {
                    deselect()
                }
                pos.sc()?.let { disconnectSync(it) }
                return false
            }
            if (other is IStreamClient) {
                if (maxConnection == 1) {
                    deselect()
                }
                if (canHaveMoreClientConnection() &&
                    other.acceptConnection(this)
                ) {
                    connectSync(other)
                }
                return false
            }
            return true
        }

        override fun drawConfigure() {
            super.drawConfigure()
            this.drawStreamGraphic()
        }

        override fun drawSelect() {
            this.drawStreamGraphic()
        }

        override fun getBuilding(): Building = this
        override fun getTile(): Tile = tile
        override fun getBlock(): Block = this@StreamHost
        @SendDataPack
        override fun connectSync(client: IStreamClient) {
            if (client.building.pos() !in clients) {
                configure(client.building.pos())
            }
        }
        @SendDataPack
        override fun disconnectSync(client: IStreamClient) {
            if (client.building.pos() in clients) {
                configure(client.building.pos())
            }
        }

        override fun maxClientConnection() = maxConnection
        override fun connectedClients(): OrderedSet<Int> = clients
        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            clients = read.intSet()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.intSet(clients)
        }
    }
}