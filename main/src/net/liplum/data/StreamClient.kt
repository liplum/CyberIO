package net.liplum.data

import arc.func.Prov
import arc.graphics.Color
import arc.scene.ui.layout.Table
import arc.struct.ObjectSet
import arc.struct.Seq
import arc.util.Eachable
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.type.Liquid
import mindustry.world.Block
import mindustry.world.blocks.ItemSelection
import mindustry.world.blocks.liquid.LiquidBlock
import mindustry.world.meta.BlockGroup
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.api.cyber.*
import net.liplum.common.delegate.Delegate1
import net.liplum.common.persistence.read
import net.liplum.common.persistence.write
import plumy.core.ClientOnly
import net.liplum.utils.sub
import net.liplum.utils.addStateMachineInfo
import net.liplum.utils.update
import plumy.animation.ContextDraw.Draw
import plumy.animation.ContextDraw.DrawOn
import plumy.animation.state.IStateful
import plumy.animation.state.State
import plumy.animation.state.StateConfig
import plumy.animation.state.configuring
import plumy.core.Serialized
import plumy.core.assets.EmptyTR
import plumy.dsl.config
import plumy.dsl.configNull

open class StreamClient(name: String) : Block(name),IDataBlock {
    @JvmField var maxConnection = -1
    @ClientOnly var NoPowerTR = EmptyTR
    @ClientOnly var BottomTR = EmptyTR
    @JvmField var dumpScale = 2f
    @ClientOnly var liquidPadding = 0f
    @ClientOnly var stateMachineConfig = StateConfig<ClientBuild>()

    init {
        buildType = Prov { ClientBuild() }
        hasLiquids = true
        update = true
        solid = true
        group = BlockGroup.liquids
        outputsLiquid = true
        configurable = true
        schematicPriority = 25
        saveConfig = true
        noUpdateDisabled = true
        canOverdrive = false
        sync = true
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            addStateMachineInfo<ClientBuild>()
            addHostInfo<ClientBuild>()
        }
    }

    override fun setStats() {
        super.setStats()
        addMaxHostStats(maxConnection)
    }

    override fun load() {
        super.load()
        BottomTR = this.sub("bottom")
        NoPowerTR = loadNoPower()
    }

    override fun init() {
        super.init()
        config<ClientBuild, Liquid> {
            outputLiquid = it
        }
        configNull<ClientBuild> {
            outputLiquid = null
        }
        ClientOnly {
            configAnimationStateMachine()
        }
    }

    override fun icons() = arrayOf(BottomTR, region)
    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        this.drawLinkedLineToClientWhenConfiguring(x, y)
    }

    override fun drawPlanConfig(req: BuildPlan, list: Eachable<BuildPlan>) {
        drawPlanConfigCenter(req, req.config, "center", true)
    }

    open inner class ClientBuild : Building(), IStateful<ClientBuild>, IStreamClient {
        override val stateMachine by lazy { stateMachineConfig.instantiate(this) }
        @Serialized
        var hosts = ObjectSet<Int>()
        @Serialized
        var outputLiquid: Liquid? = null
            set(value) {
                if (field != value) {
                    field = value
                    onRequirementUpdated(this)
                }
            }
        override val onRequirementUpdated: Delegate1<IStreamClient> = Delegate1()
        override val requirements: Seq<Liquid>?
            get() = outputLiquid.req
        override val connectedHosts: ObjectSet<Int> = hosts
        override val maxHostConnection = maxConnection
        override val clientColor: Color
            get() = outputLiquid?.fluidColor ?: R.C.Client
        var lastTileChange = -2
        override fun updateTile() {
            // Check connection every second
            if (lastTileChange != Vars.world.tileChanges) {
                lastTileChange = Vars.world.tileChanges
                checkHostsPos()
            }
            val outputLiquid = outputLiquid
            if (outputLiquid != null) {
                if (liquids.currentAmount() > 0.001f && timer(timerDump, 1f)) {
                    dumpLiquid(outputLiquid, dumpScale)
                }
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
            return if (liquid == outputLiquid)
                liquidCapacity - liquids[outputLiquid]
            else
                0f
        }

        override fun drawSelect() {
            whenNotConfiguringP2P {
                this.drawStreamGraph()
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
            hosts.write(write)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            outputLiquid = Vars.content.liquid(read.s().toInt())
            hosts.read(read)
        }

        override fun draw() {
            stateMachine.update(delta())
            BottomTR.DrawOn(this)
            LiquidBlock.drawTiledFrames(
                size, x, y, liquidPadding,
                liquids.current(), liquids.currentAmount() / liquidCapacity
            )
            region.DrawOn(this)
            stateMachine.draw()
        }
    }

    @ClientOnly lateinit var NormalState: State<ClientBuild>
    @ClientOnly lateinit var NoPowerState: State<ClientBuild>
    @ClientOnly
    fun configAnimationStateMachine() {
        NormalState = State("Normal")
        NoPowerState = State("NoPower") {
            NoPowerTR.Draw(x, y)
        }
        stateMachineConfig.configuring {
            NormalState {
                setDefaultState
                NoPowerState { !canConsume() }
            }
            NoPowerState {
                NormalState { canConsume() }
            }
        }
    }
}