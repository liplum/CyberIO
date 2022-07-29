package net.liplum.blocks.stream

import arc.func.Prov
import arc.graphics.Color
import arc.math.geom.Point2
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.logic.LAccess
import mindustry.type.Liquid
import mindustry.world.blocks.liquid.LiquidBlock
import mindustry.world.meta.BlockGroup
import net.liplum.*
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.common.persistence.read
import net.liplum.common.persistence.write
import net.liplum.lib.Serialized
import net.liplum.lib.assets.EmptyTR
import net.liplum.lib.assets.TRs
import net.liplum.mdt.*
import net.liplum.mdt.animation.anis.AniState
import net.liplum.mdt.animation.anis.config
import net.liplum.mdt.render.Draw
import net.liplum.mdt.render.DrawOn
import net.liplum.mdt.utils.*
import net.liplum.util.*
import java.util.*

private typealias AniStateH = AniState<StreamHost, StreamHost.HostBuild>

open class StreamHost(name: String) : AniedBlock<StreamHost, StreamHost.HostBuild>(name) {
    @ClientOnly var liquidPadding = 0f
    @JvmField var maxConnection = 5
    @JvmField var liquidColorLerp = 0.5f
    @JvmField var powerUseBase = 1f
    @JvmField var powerUsePerConnection = 1f
    /**
     * 1 networkSpeed = 60 per seconds
     */
    @JvmField var networkSpeed = 1f
    @JvmField var SharedClientSeq: Seq<IStreamClient> = Seq(
        if (maxConnection == -1) 10 else maxConnection
    )
    /**
     * The max range when trying to connect. -1f means no limit.
     */
    @JvmField var maxRange = -1f
    @ClientOnly var BottomTR = EmptyTR
    @ClientOnly var NoPowerTR = EmptyTR
    @ClientOnly lateinit var NoPowerAni: AniStateH
    @ClientOnly lateinit var NormalAni: AniStateH
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime
    @JvmField val TransferTimer = timers++

    init {
        buildType = Prov { HostBuild() }
        update = true
        solid = true
        configurable = true
        outputsLiquid = false
        group = BlockGroup.liquids
        noUpdateDisabled = true
        hasLiquids = true
        schematicPriority = 20
        saveConfig = true
        callDefaultBlockDraw = false
        canOverdrive = true
        sync = true
        /**
         * For connect
         */
        config(Integer::class.java) { obj: HostBuild, clientPackedPos ->
            obj.setClient(clientPackedPos.toInt())
        }
        configClear<HostBuild> {
            it.clearClients()
        }
        /**
         * For schematic
         */
        config(Array<Point2>::class.java) { obj: HostBuild, relatives ->
            obj.resolveRelativePosFromRemote(relatives)
        }
    }

    override fun load() {
        super.load()
        BottomTR = this.sub("bottom")
        NoPowerTR = this.inMod("rs-no-power")
    }

    override fun icons(): TRs {
        return arrayOf(BottomTR, region)
    }

    open fun initPowerUse() {
        consumePowerDynamic<HostBuild> {
            powerUseBase + it.clients.size * powerUsePerConnection
        }
    }

    override fun init() {
        initPowerUse()
        super.init()
    }

    override fun setStats() {
        super.setStats()
        addPowerUseStats()
        addLinkRangeStats(maxRange)
        addMaxClientStats(maxConnection)
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            addClientInfo<HostBuild>()
        }
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        drawPlacingMaxRange(x, y, maxRange, R.C.Host)
    }

    open inner class HostBuild : AniedBuild(), IStreamHost {
        override val maxRange = this@StreamHost.maxRange
        @Serialized
        var clients = OrderedSet<Int>()
        /**
         * When this stream host was restored by schematic, it should check whether the client was built.
         *
         * It contains absolute points.
         */
        var queue = LinkedList<Point2>()
        val realNetworkSpeed: Float
            get() = networkSpeed * timeScale
        override val hostColor: Color
            get() = liquids.current().fluidColor

        var lastTileChange = -2
        override fun updateTile() {
            checkQueue()
            // Check connection only when any block changed
            if (lastTileChange != Vars.world.tileChanges) {
                lastTileChange = Vars.world.tileChanges
                checkClientsPos()
            }
            if (efficiency > 0f && timer(TransferTimer, 1f)) {
                SharedClientSeq.clear()
                for (pos in clients) {
                    val client = pos.sc()
                    if (client != null) {
                        SharedClientSeq.add(client)
                    }
                }
                val liquid = liquids.current()
                val needPumped = (realNetworkSpeed * efficiency).coerceAtMost(liquids.currentAmount())
                var restNeedPumped = needPumped
                var per = restNeedPumped / clients.size
                var resetClient = clients.size
                for (client in SharedClientSeq) {
                    if (liquid.match(client.requirements)) {
                        val rest = streamTo(client, liquid, per)
                        restNeedPumped -= (per - rest)
                    }
                    resetClient--
                    if (resetClient > 0) {
                        per = restNeedPumped / resetClient
                    }
                }
                liquids.remove(liquid, needPumped - restNeedPumped)
            }
        }

        open fun checkQueue() {
            if (queue.isNotEmpty()) {
                val it = queue.iterator()
                while (it.hasNext()) {
                    val pos = it.next()
                    val dr = pos.sc()
                    if (dr != null) {
                        connectToSync(dr)
                        it.remove()
                    }
                }
            }
        }
        @CalledBySync
        fun resolveRelativePosFromRemote(relatives: Array<Point2>) {
            for (relative in relatives) {
                val rel = relative.cpy()
                rel.x += tile.x
                rel.y += tile.y
                val abs = rel
                val dr = abs.sc()
                if (dr != null) {
                    dr.onConnectFrom(this)
                    this.connectClient(dr)
                } else {
                    queue.add(abs)
                }
            }
        }

        fun genRelativeAllPos(): Array<Point2> {
            return clients.map {
                it.unpack().apply {
                    x -= tile.x
                    y -= tile.y
                }
            }.toTypedArray()
        }

        override fun config(): Any? {
            return genRelativeAllPos()
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
            return canConsume() && (liquids.current() == liquid && liquids[liquid] < liquidCapacity) || liquids.currentAmount() < 0.2f
        }
        @CalledBySync
        open fun setClient(pos: Int) {
            if (pos in clients) {
                pos.sc()?.let {
                    disconnectClient(it)
                    it.onDisconnectFrom(this)
                }
            } else {
                pos.sc()?.let {
                    connectClient(it)
                    it.onConnectFrom(this)
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
                    it.onDisconnectFrom(this)
                    it.onRequirementUpdated -= ::onClientRequirementsUpdated
                }
            }
            clients.clear()
            onClientsChanged()
        }

        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            val pos = other.pos()
            if (pos in clients) {
                if (maxConnection == 1) {
                    deselect()
                }
                pos.sc()?.let { disconnectFromSync(it) }
                return false
            }
            if (other is IStreamClient) {
                if (maxRange > 0f && other.dst(this) >= maxRange) {
                    postOverRangeOn(other)
                } else {
                    if (maxConnection == 1) {
                        deselect()
                    }
                    if (canHaveMoreClientConnection) {
                        if (other.acceptConnectionTo(this)) {
                            connectToSync(other)
                        } else {
                            postFullHostOn(other)
                        }
                    } else {
                        postFullClientOn(other)
                    }
                }
                return false
            }
            return true
        }

        override fun drawConfigure() {
            super.drawConfigure()
            this.drawStreamGraph()
            drawConfiguringMaxRange()
        }

        override fun drawSelect() {
            this.drawStreamGraph()
            drawSelectedMaxRange()
        }
        @SendDataPack
        override fun connectToSync(client: IStreamClient) {
            if (client.building.pos() !in clients) {
                configure(client.building.pos())
            }
        }
        @SendDataPack
        override fun disconnectFromSync(client: IStreamClient) {
            if (client.building.pos() in clients) {
                configure(client.building.pos())
            }
        }

        override val maxClientConnection = maxConnection
        override val connectedClients: OrderedSet<Int> = clients
        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            clients.read(read)
        }

        override fun write(write: Writes) {
            super.write(write)
            clients.write(write)
        }

        override fun fixedDraw() {
            BottomTR.DrawOn(this)
            LiquidBlock.drawTiledFrames(
                size, x, y, liquidPadding,
                liquids.current(), liquids.currentAmount() / liquidCapacity
            )
            region.DrawOn(this)
        }

        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot ->
                    if (p1 is IStreamClient) connectToSync(p1)
                else -> super.control(type, p1, p2, p3, p4)
            }
        }

        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot -> {
                    val receiver = buildAt(p1, p2)
                    if (receiver is IStreamClient) connectToSync(receiver)
                }
                else -> super.control(type, p1, p2, p3, p4)
            }
        }
    }

    override fun genAniState() {
        NoPowerAni = addAniState("NoPower") {
            NoPowerTR.Draw(x, y)
        }
        NormalAni = addAniState("Normal")
    }

    override fun genAniConfig() {
        config {
            From(NoPowerAni) To NormalAni When {
                canConsume()
            }

            From(NormalAni) To NoPowerAni When {
                !canConsume()
            }
        }
    }
}