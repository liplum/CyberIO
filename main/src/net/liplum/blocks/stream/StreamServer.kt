package net.liplum.blocks.stream

import arc.func.Prov
import arc.graphics.Color
import arc.math.geom.Point2
import arc.scene.ui.layout.Table
import arc.struct.ObjectSet
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.entities.Fires
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.logic.LAccess
import mindustry.type.Liquid
import mindustry.ui.Bar
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.api.cyber.*
import net.liplum.lib.arc.isNotEmpty
import net.liplum.lib.Serialized
import net.liplum.common.delegates.Delegate1
import net.liplum.common.persistence.read
import net.liplum.common.persistence.write
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.DrawOn
import net.liplum.mdt.WhenTheSameTeam
import net.liplum.mdt.mixin.total
import net.liplum.mdt.ui.bars.appendDisplayLiquidsDynamic
import net.liplum.mdt.ui.bars.genAllLiquidBars
import net.liplum.mdt.ui.bars.removeLiquidInBar
import net.liplum.mdt.utils.ForProximity
import net.liplum.mdt.utils.buildAt
import net.liplum.utils.addHostInfo

/**
 * ### Since 1
 * Steam server is also a [IStreamClient].
 */
open class StreamServer(name: String) : StreamHost(name) {
    @JvmField var fireproof = false
    @ClientOnly lateinit var allLiquidBars: Array<(Building) -> Bar>

    init {
        buildType = Prov { ServerBuild() }
        callDefaultBlockDraw = false
        saveConfig = false
        configurations.remove(Array<Point2>::class.java)
    }

    override fun initPowerUse() {
        consumePowerDynamic<ServerBuild> {
            powerUseBase + (it.clients.size + it.hosts.size) * powerUsePerConnection
        }
    }

    override fun init() {
        super.init()
        allLiquidBars = genAllLiquidBars()
    }

    override fun setBars() {
        super.setBars()
        removeLiquidInBar()
        DebugOnly {
            addHostInfo<ServerBuild>()
        }
    }

    override fun setStats() {
        super.setStats()
        addMaxHostStats(maxConnection)
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        this.drawLinkedLineToClientWhenConfiguring(x, y)
    }

    open inner class ServerBuild : HostBuild(), IStreamClient {
        override fun version() = 1.toByte()
        @ClientOnly @JvmField var mixedLiquidColor: Color = Color.white.cpy()
        @ClientOnly @JvmField var _hostColor: Color = R.C.Host.cpy()
        @ClientOnly
        override fun getHostColor() = _hostColor
        @ClientOnly
        fun updateHostColor() {
            _hostColor = R.C.Host.cpy()
            when (clients.size) {
                0 -> _hostColor = R.C.Host
                1 -> _hostColor = clients.first().sc()?.clientColor ?: R.C.Host
                else -> {
                    clients.forEach { pos ->
                        pos.sc()?.let {
                            if (it.clientColor != R.C.Client) {
                                _hostColor.lerp(it.clientColor, liquidColorLerp)
                            }
                        }
                    }
                }
            }
        }

        override fun onClientRequirementsUpdated(client: IStreamClient) {
            super.onClientRequirementsUpdated(client)
            ClientOnly {
                updateHostColor()
            }
        }

        override fun onClientsChanged() {
            super.onClientsChanged()
            ClientOnly {
                updateHostColor()
            }
        }

        @JvmField protected var restored = true
        override fun updateTile() {
            if (restored) {
                ClientOnly {
                    updateHostColor()
                }
                restored = false
            }
            // Check connection every second
            if (timer(CheckConnectionTimer, 60f)) {
                checkClientsPos()
                checkHostsPos()
                ClientOnly {
                    mixedLiquidColor = Color.white.cpy()
                    val total = liquids.total()
                    for (liquid in Vars.content.liquids()) {
                        val curAmount = liquids[liquid]
                        val proportion = curAmount / total
                        if (proportion > 0.2f) {
                            mixedLiquidColor.lerp(liquid.color, proportion)
                        }
                    }
                }
            }
            if (timer(TransferTimer, 1f)) {
                if (fireproof) {
                    ForProximity(5) {
                        Fires.remove(tile)
                    }
                }
                //Generate clients
                SharedClientSeq.clear()
                for (pos in clients) {
                    val client = pos.sc()
                    if (client != null) {
                        SharedClientSeq.add(client)
                    }
                }
                val needPumped = (realNetworkSpeed * efficiency).coerceAtMost(liquids.total())
                var restNeedPumped = needPumped
                var per = restNeedPumped / clients.size
                var restClient = clients.size
                for (client in SharedClientSeq) {
                    val reqs = client.requirements
                    if (reqs == null) {
                        val current = liquids.current()
                        val pumpedThisTime = per.coerceAtMost(liquids.currentAmount())
                        if (pumpedThisTime > 0.01f) {
                            val rest = streaming(client, current, pumpedThisTime)
                            val consumed = (pumpedThisTime - rest)
                            liquids.remove(current, consumed)
                            restNeedPumped -= consumed
                        }
                    } else if (reqs.isNotEmpty()) {
                        val perThisTime = per / reqs.size
                        for (liquidNeed in reqs) {
                            val pumpedThisTime = perThisTime.coerceAtMost(liquids.get(liquidNeed))
                            if (pumpedThisTime > 0.01f) {
                                val rest = streaming(client, liquidNeed!!, pumpedThisTime)
                                val consumed = (perThisTime - rest)
                                liquids.remove(liquidNeed, consumed)
                                restNeedPumped -= consumed
                            }
                        }
                    }
                    restClient--
                    if (restClient > 0) {
                        per = restNeedPumped / restClient
                    }
                }
                val consumed = needPumped - restNeedPumped
                ClientOnly {
                    liquidFlow += consumed
                }
            }
        }

        override fun acceptLiquid(source: Building, liquid: Liquid): Boolean {
            return canConsume() && liquids.get(liquid) < liquidCapacity
        }

        override fun handleLiquid(source: Building, liquid: Liquid, amount: Float) {
            super.handleLiquid(source, liquid, amount)
            ClientOnly {
                liquidFlow += amount
            }
        }

        override fun fixedDraw() {
            region.DrawOn(this)
            Drawf.liquid(
                LiquidTR, x, y,
                liquids.total() / liquidCapacity,
                mixedLiquidColor,
                0f
            )
            TopTR.DrawOn(this)
            drawTeamTop()
        }

        override fun drawSelect() {
            super.drawSelect()
            whenNotConfiguringHost {
                (this as IStreamClient).drawStreamGraphic()
            }
            this.drawRequirements()
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
        @Serialized
        var hosts = OrderedSet<Int>()
        override fun readStream(host: IStreamHost, liquid: Liquid, amount: Float) {
            if (this.isConnectedWith(host)) {
                liquids.add(liquid, amount)
                ClientOnly {
                    liquidFlow += amount
                }
            }
        }

        override fun acceptedAmount(host: IStreamHost, liquid: Liquid): Float {
            if (!canConsume()) return 0f
            if (!isConnectedWith(host)) return 0f
            return liquidCapacity - liquids[liquid]
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            val version = revision.toInt()
            if (version > 0) {
                // Since 1
                hosts.read(read)
            }
        }

        override fun afterRead() {
            restored = true
        }

        override fun write(write: Writes) {
            super.write(write)
            hosts.write(write)
        }

        override fun displayBars(table: Table) {
            super.displayBars(table)
            WhenTheSameTeam {
                this.appendDisplayLiquidsDynamic(
                    table, allLiquidBars
                ) {
                    super.displayBars(table)
                }
            }
        }

        @JvmField var onRequirementUpdated: Delegate1<IStreamClient> = Delegate1()
        override fun getOnRequirementUpdated() = onRequirementUpdated
        override fun getRequirements(): Seq<Liquid>? = null
        override fun getConnectedHosts(): ObjectSet<Int> = hosts
        override fun maxHostConnection() = maxConnection
        override fun getClientColor(): Color = mixedLiquidColor
    }
}