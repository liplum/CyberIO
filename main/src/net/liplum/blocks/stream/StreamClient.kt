package net.liplum.blocks.stream

import arc.graphics.Color
import arc.graphics.g2d.TextureRegion
import arc.scene.ui.layout.Table
import arc.struct.ObjectSet
import arc.struct.OrderedSet
import arc.util.Eachable
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.type.Liquid
import mindustry.world.blocks.ItemSelection
import mindustry.world.blocks.liquid.LiquidBlock
import mindustry.world.meta.BlockGroup
import net.liplum.mdt.ClientOnly
import net.liplum.DebugOnly
import net.liplum.lib.Serialized
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.mdt.Draw
import net.liplum.mdt.DrawOn
import net.liplum.mdt.animations.anis.AniState
import net.liplum.mdt.animations.anis.config
import net.liplum.lib.delegates.Delegate1
import net.liplum.lib.persistance.intSet
import net.liplum.lib.TR
import net.liplum.utils.addHostInfo
import net.liplum.mdt.utils.inMod
import net.liplum.mdt.utils.sub

private typealias AniStateC = AniState<StreamClient, StreamClient.ClientBuild>

open class StreamClient(name: String) : AniedBlock<StreamClient, StreamClient.ClientBuild>(name) {
    @JvmField var maxConnection = -1
    @ClientOnly lateinit var NoPowerTR: TR
    @ClientOnly lateinit var LiquidTR: TR
    @ClientOnly lateinit var TopTR: TR
    @JvmField val timerTransfer = timers++
    @JvmField var dumpScale = 2f

    init {
        hasLiquids = true
        update = true
        solid = true
        group = BlockGroup.liquids
        outputsLiquid = true
        configurable = true
        saveConfig = true
        noUpdateDisabled = true
        canOverdrive = false
        sync = true
        config(
            Liquid::class.java
        ) { obj: ClientBuild, liquid ->
            obj.outputLiquid = liquid
        }
        configClear { tile: ClientBuild -> tile.outputLiquid = null }
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            addHostInfo<ClientBuild>()
        }
    }

    override fun load() {
        super.load()
        NoPowerTR = this.inMod("rs-no-power")
        LiquidTR = this.sub("liquid")
        TopTR = this.sub("top")
    }

    override fun icons(): Array<TextureRegion> {
        return arrayOf(region, TopTR)
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        this.drawLinkedLineToClientWhenConfiguring(x, y)
    }

    override fun drawPlanConfig(req: BuildPlan, list: Eachable<BuildPlan>) {
        drawPlanConfigCenter(req, req.config, "center", true)
    }

    open inner class ClientBuild : AniedBuild(), IStreamClient {
        @Serialized
        var hosts = OrderedSet<Int>()
        @Serialized
        var outputLiquid: Liquid? = null
            set(value) {
                if (field != value) {
                    field = value
                    onRequirementUpdated(this)
                }
            }

        open fun checkHostPos() {
            hosts.removeAll { !it.sh().exists }
        }

        @JvmField var onRequirementUpdated: Delegate1<IStreamClient> = Delegate1()
        override fun getOnRequirementUpdated() = onRequirementUpdated
        override fun getRequirements(): Array<Liquid>? = outputLiquid.req
        override fun getConnectedHosts(): ObjectSet<Int> = hosts
        override fun maxHostConnection() = maxConnection
        override fun getClientColor(): Color = outputLiquid.clientColor
        override fun updateTile() {
            // Check connection every second
            if (Time.time % 60f < 1) {
                checkHostPos()
            }
            val outputLiquid = outputLiquid
            // every tick check dump
            if (outputLiquid != null) {
                if (efficiency > 0f && liquids.currentAmount() > 0.001f
                    && timer(timerTransfer, 1f)
                ) {
                    dumpLiquid(outputLiquid, dumpScale * efficiency)
                }
            }
        }

        override fun readStream(host: IStreamHost, liquid: Liquid, amount: Float) {
            if (this.isConnectedWith(host)) {
                liquids.add(liquid, amount)
            }
        }

        override fun acceptedAmount(host: IStreamHost, liquid: Liquid): Float {
            if (!canConsume()) return 0f
            if (!isConnectedWith(host)) return 0f
            return if (liquid == outputLiquid)
                liquidCapacity - liquids[outputLiquid]
            else
                0f
        }

        override fun drawSelect() {
            whenNotConfiguringHost {
                this.drawStreamGraphic()
            }
            this.drawRequirements()
        }

        override fun acceptLiquid(source: Building, liquid: Liquid) = false
        override fun buildConfiguration(table: Table?) {
            ItemSelection.buildTable(this@StreamClient, table, Vars.content.liquids(),
                { outputLiquid },
                { value: Liquid? -> tryConfigOutputLiquid(value) })
        }

        open fun tryConfigOutputLiquid(liquid: Liquid?): Boolean {
            if (liquids.currentAmount() > 0.1f) {
                return false
            }
            configure(liquid)
            return true
        }

        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            return true
        }

        override fun config(): Liquid? = outputLiquid
        override fun write(write: Writes) {
            super.write(write)
            val outputLiquid = outputLiquid
            write.s(outputLiquid?.id?.toInt() ?: -1)
            write.intSet(hosts)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            outputLiquid = Vars.content.liquid(read.s().toInt())
            hosts = read.intSet()
        }

        override fun fixedDraw() {
            LiquidBlock.drawTiledFrames(
                size, x, y, 0f,
                liquids.current(), liquids.currentAmount() / liquidCapacity
            )
            TopTR.DrawOn(this)
        }
    }

    @ClientOnly lateinit var NormalAni: AniStateC
    @ClientOnly lateinit var NoPowerAni: AniStateC
    override fun genAniState() {
        NormalAni = addAniState("Normal") {
        }
        NoPowerAni = addAniState("NoPower") {
            NoPowerTR.Draw(x, y)
        }
    }

    override fun genAniConfig() {
        config {
            From(NormalAni) To NoPowerAni When {
                !canConsume()
            }
            From(NoPowerAni) To NormalAni When {
                canConsume()
            }
        }
    }
}