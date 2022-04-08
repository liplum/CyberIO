package net.liplum.blocks.stream

import arc.graphics.Color
import arc.util.Time
import mindustry.Vars
import mindustry.entities.Fires
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.logic.LAccess
import mindustry.type.Liquid
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.lib.animations.anis.DrawOn
import net.liplum.lib.animations.anis.DrawRotateOn
import net.liplum.api.cyber.IStreamClient
import net.liplum.api.cyber.sc
import net.liplum.utils.ForProximity
import net.liplum.utils.buildAt

open class StreamServer(name: String) : StreamHost(name) {
    @JvmField var fireproof = false

    init {
        callDefaultBlockDraw = false
    }

    open inner class ServerBuild : HostBuild() {
        @ClientOnly @JvmField var mixedLiquidColor: Color = Color.white.cpy()
        @ClientOnly @JvmField var _hostColor: Color = R.C.Host.cpy()
        @ClientOnly
        override fun getHostColor() = _hostColor
        @ClientOnly
        fun updateHostColor() {
            ClientOnly {
                _hostColor = R.C.Host.cpy()
                clients.forEach { pos ->
                    pos.sc()?.let {
                        if (it.clientColor != R.C.Client) {
                            _hostColor.lerp(it.clientColor, liquidColorLerp)
                        }
                    }
                }
            }
        }

        override fun onClientRequirementsUpdated(client: IStreamClient) {
            super.onClientRequirementsUpdated(client)
            updateHostColor()
        }

        override fun onClientsChanged() {
            super.onClientsChanged()
            updateHostColor()
        }

        override fun updateTile() {
            // Check connection every second
            if (Time.time % 60f < 1) {
                checkClientsPos()
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
            if (!consValid()) return
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
            val needPumped = realNetworkSpeed.coerceAtMost(liquids.total())
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

        override fun acceptLiquid(source: Building, liquid: Liquid): Boolean {
            return consValid() && liquids.get(liquid) < liquidCapacity
        }

        override fun handleLiquid(source: Building, liquid: Liquid, amount: Float) {
            super.handleLiquid(source, liquid, amount)
            ClientOnly {
                liquidFlow += amount
            }
        }

        override fun fixedDraw() {
            BaseTR.DrawOn(this)
            Drawf.liquid(
                LiquidTR, x, y,
                liquids.total() / liquidCapacity,
                mixedLiquidColor,
                (rotation - 90).toFloat()
            )
            TopTR.DrawRotateOn(this, liquidFlow)
            drawTeamTop()
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
}