package net.liplum.data

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
import net.liplum.common.delegate.Delegate1
import net.liplum.common.persistence.read
import net.liplum.common.persistence.write
import plumy.core.Serialized
import plumy.core.arc.isNotEmpty
import plumy.core.assets.EmptyTR
import plumy.core.ClientOnly
import net.liplum.utils.WhenTheSameTeam
import net.liplum.mixin.total
import plumy.animation.ContextDraw.DrawOn
import net.liplum.ui.bars.appendDisplayLiquidsDynamic
import net.liplum.ui.bars.genAllLiquidBars
import net.liplum.ui.bars.removeLiquidInBar
import net.liplum.utils.ForProximity
import net.liplum.utils.sub
import net.liplum.utils.update
import plumy.dsl.build

/**
 * ### Since 1
 * Steam server is also a [IStreamClient].
 */
open class StreamServer(name: String) : StreamHost(name) {
    @JvmField var fireproof = false
    @ClientOnly lateinit var allLiquidBars: Array<(Building) -> Bar>
    @ClientOnly var LiquidTR = EmptyTR
    @JvmField val ColorUpdateTimer = timers++
    var ColorUpdateTime = if (Vars.mobile) 120f else 60f

    init {
        buildType = Prov { ServerBuild() }
        saveConfig = false
        configurations.remove(Array<Point2>::class.java)
    }

    override fun initPowerUse() {
        consumePowerDynamic<ServerBuild> {
            powerUseBase + (it.clients.size + it.hosts.size) * powerUsePerConnection
        }
    }

    override fun load() {
        super.load()
        LiquidTR = this.sub("liquid")
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

    open inner class ServerBuild : HostBuild(), IStreamServer {
        @Serialized
        var hosts = OrderedSet<Int>()
        @ClientOnly @JvmField var mixedFluidColor = R.C.Host
        @ClientOnly
        override val hostColor: Color
            get() = mixedFluidColor
        override val clientColor: Color
            get() = mixedFluidColor
        @ClientOnly
        fun updateColor() {
            mixedFluidColor = Color.gray.cpy()
            val total = liquids.total()
            for (liquid in Vars.content.liquids()) {
                val curAmount = liquids[liquid]
                val proportion = curAmount / total
                if (curAmount > 0.0001f) {
                    mixedFluidColor.lerp(liquid.fluidColor, proportion)
                }
            }
        }

        override fun updateTile() {
            // Check connection only when any block changed
            if (lastTileChange != Vars.world.tileChanges) {
                lastTileChange = Vars.world.tileChanges
                checkClientsPos()
                checkHostsPos()
            }
            if (efficiency > 0f && timer(TransferTimer, 1f)) {
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
                            val rest = streamTo(client, current, pumpedThisTime)
                            val consumed = (pumpedThisTime - rest)
                            liquids.remove(current, consumed)
                            restNeedPumped -= consumed
                        }
                    } else if (reqs.isNotEmpty()) {
                        val perThisTime = per / reqs.size
                        for (liquidNeed in reqs) {
                            val pumpedThisTime = perThisTime.coerceAtMost(liquids.get(liquidNeed))
                            if (pumpedThisTime > 0.01f) {
                                val rest = streamTo(client, liquidNeed!!, pumpedThisTime)
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
            }
            if (timer(ColorUpdateTimer, ColorUpdateTime)) {
                ClientOnly {
                    updateColor()
                }
            }
        }

        override fun acceptLiquid(source: Building, liquid: Liquid): Boolean {
            return canConsume() && liquids.get(liquid) < liquidCapacity
        }

        override fun draw() {
            stateMachine.update(delta())
            BottomTR.DrawOn(this)
            Drawf.liquid(
                LiquidTR, x, y,
                liquids.total() / liquidCapacity,
                mixedFluidColor,
                0f
            )
            region.DrawOn(this)
            stateMachine.draw()
        }

        override fun drawSelect() {
            super.drawSelect()
            whenNotConfiguringP2P {
                (this as IStreamClient).drawStreamGraph()
            }
            this.drawRequirements()
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
                    val receiver = Vars.world.build(p1, p2)
                    if (receiver is IStreamClient) connectToSync(receiver)
                }
                else -> super.control(type, p1, p2, p3, p4)
            }
        }

        override fun readStreamFrom(host: IStreamHost, liquid: Liquid, amount: Float) {
            if (this.isConnectedTo(host)) {
                liquids.add(liquid, amount)
            }
        }

        override fun getAcceptedAmount(host: IStreamHost, liquid: Liquid): Float {
            if (!canConsume()) return 0f
            if (!isConnectedTo(host)) return 0f
            return liquidCapacity - liquids[liquid]
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            hosts.read(read)
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

        override val onRequirementUpdated: Delegate1<IStreamClient> = Delegate1()
        override val requirements: Seq<Liquid>? = null
        override val connectedHosts: ObjectSet<Int> = hosts
        override val maxHostConnection = maxConnection
    }
}