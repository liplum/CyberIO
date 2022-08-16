package net.liplum.blocks.stream

import arc.func.Prov
import arc.graphics.Color
import arc.math.geom.Point2
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.Eachable
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.logic.LAccess
import mindustry.type.Liquid
import mindustry.world.blocks.liquid.LiquidBlock
import mindustry.world.meta.BlockGroup
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.common.persistence.read
import net.liplum.common.persistence.write
import net.liplum.mdt.CalledBySync
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.SendDataPack
import net.liplum.mdt.animation.state.State
import net.liplum.mdt.animation.state.configStates
import net.liplum.mdt.render.Draw
import net.liplum.mdt.render.DrawOn
import net.liplum.mdt.utils.*
import net.liplum.util.addPowerUseStats
import plumy.core.Serialized
import plumy.core.assets.EmptyTR
import plumy.core.assets.TRs
import plumy.world.*

private typealias AniStateH = State<StreamHost.HostBuild>

open class StreamHost(name: String) : AniedBlock<StreamHost.HostBuild>(name) {
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
        callDefaultBlockDraw = false
        canOverdrive = true
        sync = true
        // For connect
        config<HostBuild, PackedPos> {
            setClient(it)
        }
        configNull<HostBuild> {
            clearClients()
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
        addDataTransferSpeedStats(networkSpeed)
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

    override fun drawPlanRegion(plan: BuildPlan, list: Eachable<BuildPlan>) {
        super.drawPlanRegion(plan, list)
        drawPlanMaxRange(plan.x, plan.y, maxRange, R.C.Host)
    }

    open inner class HostBuild : AniedBuild(), IStreamHost {
        override val maxRange = this@StreamHost.maxRange
        @Serialized
        var clients = OrderedSet<Int>()
        val realNetworkSpeed: Float
            get() = networkSpeed * timeScale
        override val hostColor: Color
            get() = liquids.current().fluidColor
        var lastTileChange = -2
        override fun updateTile() {
            // Check connection and queue only when any block changed
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

        fun genRelativeAllPos(): Array<Point2> {
            return clients.map {
                it.unpack().apply {
                    x -= tile.x
                    y -= tile.y
                }
            }.toTypedArray()
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
        configStates {
            From(NoPowerAni) To NormalAni When {
                canConsume()
            }

            From(NormalAni) To NoPowerAni When {
                !canConsume()
            }
        }
    }
}