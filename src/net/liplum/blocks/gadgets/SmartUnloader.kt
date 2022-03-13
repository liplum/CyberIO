package net.liplum.blocks.gadgets

import arc.graphics.g2d.Draw
import arc.struct.OrderedSet
import mindustry.Vars
import mindustry.gen.Building
import mindustry.world.Tile
import mindustry.world.meta.BlockGroup
import net.liplum.ClientOnly
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.config
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender
import net.liplum.api.data.dr
import net.liplum.blocks.AniedBlock
import net.liplum.utils.TR
import net.liplum.utils.inMod
import net.liplum.utils.isZero
import kotlin.math.absoluteValue

private typealias AniStateU = AniState<SmartUnloader, SmartUnloader.SmartULDBuild>

open class SmartUnloader(name: String) : AniedBlock<SmartUnloader, SmartUnloader.SmartULDBuild>(name) {
    @ClientOnly lateinit var RequireAni: AniStateU
    @ClientOnly lateinit var NoPowerAni: AniStateU
    @JvmField var unloadSpeed = 1f
    @JvmField var maxConnection = 5
    @ClientOnly lateinit var NoPowerTR: TR

    init {
        solid = true
        update = true
        hasPower = true
        hasItems = true
        group = BlockGroup.transportation
        noUpdateDisabled = true
        unloadable = false
        canOverdrive = false
        config(Integer::class.java) { obj: SmartULDBuild, receiverPackedPos ->
            obj.setReceiver(receiverPackedPos.toInt())
        }
        configClear<SmartULDBuild> {
            it.clearReceiver()
        }
    }

    override fun load() {
        super.load()
        NoPowerTR = this.inMod("rs-no-power")
    }

    open inner class SmartULDBuild : AniedBlock<SmartUnloader, SmartUnloader.SmartULDBuild>.AniedBuild(),
        IDataSender {
        var receiversPos = OrderedSet<Int>()
        var trackers: Array<Tracker> = Array(Vars.content.items().size) {
            Tracker(maxConnection)
        }
        var unloadTimer = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }

        override fun updateTile() {
            if (!consValid()) {
                return
            }
            if (receiversPos.isEmpty) {
                return
            }
            unloadTimer += delta()
            if (unloadTimer < unloadSpeed) {
                return
            }
            unloadTimer = 0f
            unload()
        }

        open fun unload() {
        }

        open fun updateTracker() {
        }

        override fun onConfigureTileTapped(other: Building): Boolean {
            if (this === other) {
                deselect()
                configure(null)
                return false
            }
            if (other.pos() in receiversPos) {
                deselect()
                configure(-other.pos())
                return false
            }
            if (other is IDataReceiver) {
                deselect()
                if (other.acceptConnection(this)) {
                    configure(other.pos())
                }
                return false
            }
            return true
        }

        open fun setReceiver(pos: Int) {
            if (pos >= 0) {
                val dr = pos.dr()
                if (dr != null) {
                    connect(dr)
                }
            } else {
                val dr = pos.absoluteValue.dr()
                if (dr != null) {
                    disconnect(dr)
                }
            }
        }

        open fun clearReceiver() {
            receiversPos.clear()
        }

        override fun connect(receiver: IDataReceiver) {
            receiversPos.add(receiver.building.pos())
            updateTracker()
            if (receiver is SmartDistributor.SmartDISBuild) {
                receiver.onRequirementUpdated += {
                    updateTracker()
                }
            }
        }

        override fun disconnect(receiver: IDataReceiver) {
            receiversPos.remove(receiver.building.pos())
            updateTracker()
            if (receiver is SmartDistributor.SmartDISBuild) {
                receiver.onRequirementUpdated -= {
                    updateTracker()
                }
            }
        }

        override fun connectedReceiver(): Int? =
            if (receiversPos.isEmpty)
                null
            else
                receiversPos.first()

        override fun canMultipleConnect() = true
        override fun connectedReceivers(): OrderedSet<Int> = receiversPos
        override fun getBuilding() = this
        override fun getTile(): Tile = tile
        override fun getBlock() = this@SmartUnloader
    }

    override fun genAniConfig() {
        config {
            From(RequireAni) To NoPowerAni When {
                it.power.status.isZero()
            }
            From(NoPowerAni) To RequireAni When {
                !it.power.status.isZero()
            }
        }
    }

    override fun genAniState() {
        RequireAni = addAniState("Require")
        NoPowerAni = addAniState("NoPower") {
            Draw.rect(NoPowerTR, it.x, it.y)
        }
    }
}