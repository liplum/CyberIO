package net.liplum.blocks.stream

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.util.Time
import mindustry.Vars
import mindustry.entities.Fires
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.type.Liquid
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.api.stream.IStreamClient
import net.liplum.api.stream.sc
import net.liplum.utils.ForProximity
import net.liplum.utils.TR
import net.liplum.utils.sub

open class StreamServer(name: String) : StreamHost(name) {
    @ClientOnly lateinit var TankTR: TR
    @ClientOnly lateinit var CoverTR: TR
    @JvmField var fireproof = false
    override fun load() {
        super.load()
        TankTR = this.sub("tank")
        CoverTR = this.sub("cover")
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
            var needPumped = networkSpeed.coerceAtMost(liquids.total())
            var per = needPumped / clients.size
            var resetClient = clients.size
            for (client in SharedClientSeq) {
                val reqs = client.requirements
                if (reqs == null) {
                    val current = liquids.current()
                    val pumpedThisTime = per.coerceAtMost(liquids.currentAmount())
                    val rest = streaming(client, current, pumpedThisTime)
                    val consumed = (pumpedThisTime - rest)
                    liquids.remove(current, consumed)
                    needPumped -= consumed
                } else if (reqs.isNotEmpty()) {
                    val perThisTime = per / reqs.size
                    for (liquidNeed in reqs) {
                        val pumpedThisTime = perThisTime.coerceAtMost(liquids.get(liquidNeed))
                        val rest = streaming(client, liquidNeed, pumpedThisTime)
                        val consumed = (per - rest)
                        liquids.remove(liquidNeed, consumed)
                        needPumped -= consumed
                    }
                }
                resetClient--
                if (resetClient > 0) {
                    per = needPumped / resetClient
                }
            }
        }

        override fun acceptLiquid(source: Building, liquid: Liquid): Boolean {
            return liquids.get(liquid) < liquidCapacity
        }

        override fun draw() {
            Draw.rect(BaseTR, x, y)
            Drawf.liquid(
                TankTR, x, y,
                liquids.total() / liquidCapacity,
                mixedLiquidColor,
                (rotation - 90).toFloat()
            )
            Draw.rect(CoverTR, x, y)
            drawTeamTop()
        }
    }
}