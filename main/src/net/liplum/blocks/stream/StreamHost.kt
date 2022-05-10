package net.liplum.blocks.stream

import arc.graphics.Color
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.logic.LAccess
import mindustry.type.Liquid
import mindustry.world.meta.BlockGroup
import net.liplum.*
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.lib.DrawOn
import net.liplum.lib.DrawSize
import net.liplum.lib.SetAlpha
import net.liplum.lib.animations.Floating
import net.liplum.lib.animations.anis.AniState
import net.liplum.lib.animations.anis.config
import net.liplum.persistance.intSet
import net.liplum.utils.*

private typealias AniStateH = AniState<StreamHost, StreamHost.HostBuild>

open class StreamHost(name: String) : AniedBlock<StreamHost, StreamHost.HostBuild>(name) {
    @JvmField var maxConnection = 5
    @JvmField var liquidColorLerp = 0.5f
    @JvmField var powerUseBase = 1f
    @JvmField var powerUsePerConnection = 1f
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var LiquidTR: TR
    @ClientOnly lateinit var TopTR: TR
    @ClientOnly lateinit var NoPowerAni: AniStateH
    @ClientOnly lateinit var NormalAni: AniStateH
    @ClientOnly lateinit var NoPowerTR: TR
    @ClientOnly @JvmField var IconFloatingRange = 1f
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
        canOverdrive = true
        sync = true
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
        LiquidTR = this.sub("liquid")
        TopTR = this.sub("top")
        NoPowerTR = this.inMod("rs-no-power-large")
    }

    open fun initPowerUse() {
        consumePowerDynamic<HostBuild> {
            powerUseBase + it.clients.size * powerUsePerConnection
        }
    }

    override fun init() {
        initPowerUse()
        super.init()
        IconFloatingRange = IconFloatingRange / 8f * size
    }

    override fun setStats() {
        super.setStats()
        addPowerUseStats()
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            addClientInfo<HostBuild>()
        }
    }

    open inner class HostBuild : AniedBuild(), IStreamHost {
        @Serialized
        var clients = OrderedSet<Int>()
        @ClientOnly @JvmField var liquidFlow = 0f
        open fun checkClientsPos() {
            clients.removeAll { !it.sc().exists }
        }

        val realNetworkSpeed: Float
            get() = networkSpeed * timeScale
        @ClientOnly @JvmField
        var floating: Floating = Floating(IconFloatingRange).randomXY().changeRate(1)
        override fun getHostColor(): Color = liquids.current().color
        override fun updateTile() {
            // Check connection every second
            if (Time.time % 60f < 1) {
                checkClientsPos()
            }
            if (!canConsume()) return
            SharedClientSeq.clear()
            for (pos in clients) {
                val client = pos.sc()
                if (client != null) {
                    SharedClientSeq.add(client)
                }
            }
            val liquid = liquids.current()
            val needPumped = realNetworkSpeed.coerceAtMost(liquids.currentAmount())
            var restNeedPumped = needPumped
            var per = restNeedPumped / clients.size
            var resetClient = clients.size
            for (client in SharedClientSeq) {
                if (liquid.match(client.requirements)) {
                    val rest = streaming(client, liquid, per)
                    restNeedPumped -= (per - rest)
                }
                resetClient--
                if (resetClient > 0) {
                    per = restNeedPumped / resetClient
                }
            }
            liquids.remove(liquid, needPumped - restNeedPumped)
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
            return canConsume() && liquids.current() == liquid || liquids.currentAmount() < 0.2f
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

        override fun onConfigureBuildTapped(other: Building): Boolean {
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
        override fun getConnectedClients(): OrderedSet<Int> = clients
        override fun beforeDraw() {
            val d = G.D(0.1f * IconFloatingRange * delta())
            floating.move(d)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            clients = read.intSet()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.intSet(clients)
        }

        override fun fixedDraw() {
            BaseTR.DrawOn(this)
            Drawf.liquid(
                LiquidTR, x, y,
                liquids.currentAmount() / liquidCapacity,
                liquids.current().color,
                (rotation - 90).toFloat()
            )
            TopTR.DrawOn(this)
        }

        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot ->
                    if (p1 is IStreamClient) connectSync(p1)
                else -> super.control(type, p1, p2, p3, p4)
            }
        }

        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot -> {
                    val receiver = buildAt(p1, p2)
                    if (receiver is IStreamClient) connectSync(receiver)
                }
                else -> super.control(type, p1, p2, p3, p4)
            }
        }
    }

    override fun genAniState() {
        NoPowerAni = addAniState("NoPower") {
            SetAlpha(0.8f)
            NoPowerTR.DrawSize(
                x + floating.dx,
                y + floating.dy,
                1f / 7f * this@StreamHost.size
            )
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