package net.liplum.blocks.gadgets

import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.type.Item
import mindustry.ui.Bar
import mindustry.world.Tile
import mindustry.world.meta.BlockGroup
import net.liplum.*
import net.liplum.animations.anims.Animation
import net.liplum.animations.anims.AnimationObj
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.DrawTR
import net.liplum.animations.anis.config
import net.liplum.api.data.*
import net.liplum.blocks.AniedBlock
import net.liplum.persistance.intSet
import net.liplum.utils.*
import kotlin.math.absoluteValue

private typealias AniStateU = AniState<SmartUnloader, SmartUnloader.SmartULDBuild>
private typealias SmartDIS = SmartDistributor.SmartDISBuild

open class SmartUnloader(name: String) : AniedBlock<SmartUnloader, SmartUnloader.SmartULDBuild>(name) {
    @JvmField var unloadSpeed = 1f
    @JvmField var maxConnection = 5
    @ClientOnly lateinit var ShrinkingAnim: Animation
    @ClientOnly lateinit var CoverTR: TR
    @JvmField @ClientOnly var ShrinkingAnimFrames = 13
    @JvmField @ClientOnly var ShrinkingAnimDuration = 120f
    @JvmField @ClientOnly var SendingTime = 60f
    @JvmField @ClientOnly var UnloadTime = 60f
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
        unloadable = false
        itemCapacity = 50
        allowConfigInventory = false
        configurable = true
        saveConfig = true
        acceptsItems = false

        config(Integer::class.java) { obj: SmartULDBuild, receiverPackedPos ->
            obj.setReceiver(receiverPackedPos.toInt())
        }
        configClear<SmartULDBuild> {
            it.clearReceivers()
        }
    }

    override fun load() {
        super.load()
        NoPowerTR = this.inMod("rs-no-power")
        ShrinkingAnim = this.autoAnim("shrink", ShrinkingAnimFrames, ShrinkingAnimDuration)
        CoverTR = this.sub("cover")
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            bars.addReceiverInfo<SmartULDBuild>()
            bars.add<SmartULDBuild>("last-unloading") {
                Bar(
                    { "Last Unload: ${it.lastUnloadTime.toInt()}" },
                    { Pal.bar },
                    { it.lastUnloadTime / UnloadTime }
                )
            }
            bars.add<SmartULDBuild>("last-sending") {
                Bar(
                    { "Last Send: ${it.lastSendingTime.toInt()}" },
                    { Pal.bar },
                    { it.lastSendingTime / SendingTime }
                )
            }
        }
    }

    open inner class SmartULDBuild : AniedBlock<SmartUnloader, SmartUnloader.SmartULDBuild>.AniedBuild(),
        IDataSender {
        var receiversPos = OrderedSet<Int>()
        var nearby: Seq<Building> = Seq()
        var trackers: Array<Tracker> = Array(Vars.content.items().size) {
            Tracker(maxConnection)
        }
        var needUnloadItems: OrderedSet<Item> = OrderedSet()
        var unloadedNearbyIndex = 0
            set(value) {
                field = (if (nearby.isEmpty)
                    0
                else
                    value.absoluteValue % nearby.size)
            }
        var unloadTimer = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @ClientOnly var lastUnloadTime = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @ClientOnly var lastSendingTime = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @ClientOnly val isSending: Boolean
            get() = lastSendingTime < SendingTime
        @ClientOnly val isUnloading: Boolean
            get() = lastUnloadTime < UnloadTime
        @ClientOnly lateinit var shrinkingAnimObj: AnimationObj
        var restored = false
        override fun created() {
            super.created()
            ClientOnly {
                shrinkingAnimObj = ShrinkingAnim.gen()
            }
        }

        open fun updateUnloaded() {
            nearby.clear()
            for (b in proximity) {
                if (b.block.unloadable) {
                    nearby.add(b)
                }
            }
            unloadedNearbyIndex = 0
        }

        open fun checkReceiverPos() {
            receiversPos.removeAll { !it.dr().exists }
        }

        override fun updateTile() {
            if (restored) {
                updateTracker()
                restored = false
            }

            ClientOnly {
                lastUnloadTime += Time.delta
                lastSendingTime += Time.delta
            }
            if (Time.time % 60f < 1) {
                checkReceiverPos()
            }
            if (!consValid()) {
                return
            }
            if (receiversPos.isEmpty) {
                return
            }
            unloadTimer += delta()
            if (unloadTimer >= unloadSpeed) {
                unloadTimer = 0f
                val unloaded = unload()
                ClientOnly {
                    if (unloaded) {
                        lastUnloadTime = 0f
                    }
                }
            }
            val sent = sendData()
            ClientOnly {
                if (sent) {
                    lastSendingTime = 0f
                }
            }
        }

        open fun Item.canBeUnloadedToThis(): Boolean {
            return items.get(this) < getMaximumAccepted(this)
        }

        open fun unload(): Boolean {
            var unloaded = false
            if (!nearby.isEmpty) {
                val container = nearby[unloadedNearbyIndex]
                for (item in needUnloadItems) {
                    if (!item.canBeUnloadedToThis()) {
                        continue
                    }
                    if (container.items.has(item)) {
                        container.items.remove(item, 1)
                        this.items.add(item, 1)
                        unloaded = true
                    }
                }
                unloadedNearbyIndex++
            }
            return unloaded
        }

        open fun sendData(): Boolean {
            var sent = false
            for (item in needUnloadItems) {
                val tracker = trackers[item.ID]
                if (tracker.receivers.isEmpty() || !this.items.has(item))
                    continue
                val receiver = tracker.receivers[tracker.curIndex]
                if (receiver.acceptedAmount(this, item).isAccepted()) {
                    this.items.remove(item, 1)
                    this.sendData(receiver, item, 1)
                    sent = true
                }
                tracker.curIndex++
            }
            return sent
        }

        open fun clearTrackers() {
            trackers.forEach {
                it.clear()
            }
        }

        open fun onReceiverRequirementsUpdated(receiver: IDataReceiver) {
            updateTracker()
        }

        open fun updateTracker() {
            clearTrackers()
            for (receiverPos in receiversPos) {
                val receiver = receiverPos.dr()
                if (receiver != null) {
                    val reqs = receiver.requirements
                    if (reqs != null) {
                        for (req in reqs) {
                            val tracker = trackers[req.ID]
                            if (tracker.canAddMore()) {
                                needUnloadItems.add(req)
                                tracker.add(receiver)
                            }
                        }
                    }
                }
            }
        }

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            updateUnloaded()
            updateTracker()
        }

        override fun drawSelect() {
            this.drawDataNetGraphic()
        }

        override fun drawConfigure() {
            super.drawConfigure()
            this.drawDataNetGraphic()
        }

        override fun onConfigureTileTapped(other: Building): Boolean {
            if (this === other) {
                deselect()
                configure(null)
                return false
            }
            val pos = other.pos()
            if (pos in receiversPos) {
                deselect()
                pos.dr()?.let { disconnectSync(it) }
                return false
            }
            if (other is IDataReceiver) {
                deselect()
                if (receiversPos.size < maxConnection &&
                    other.acceptConnection(this)
                ) {
                    connectSync(other)
                }
                return false
            }
            return true
        }
        @CalledBySync
        open fun setReceiver(pos: Int) {
            if (pos in receiversPos) {
                pos.dr()?.let {
                    disconnectReceiver(it)
                    it.disconnect(this)
                }
            } else {
                pos.dr()?.let {
                    connectReceiver(it)
                    it.connect(this)
                }
            }
        }
        @CalledBySync
        open fun connectReceiver(receiver: IDataReceiver) {
            if (receiversPos.add(receiver.building.pos())) {
                updateTracker()
                receiver.onRequirementUpdated += ::onReceiverRequirementsUpdated
            }
        }
        @CalledBySync
        open fun disconnectReceiver(receiver: IDataReceiver) {
            if (receiversPos.remove(receiver.building.pos())) {
                updateTracker()
                receiver.onRequirementUpdated -= ::onReceiverRequirementsUpdated
            }
        }
        @CalledBySync
        open fun clearReceivers() {
            receiversPos.forEach { pos ->
                pos.dr()?.let {
                    it.disconnect(this)
                    it.onRequirementUpdated -= ::onReceiverRequirementsUpdated
                }
            }
            receiversPos.clear()
            updateTracker()
        }
        @SendDataPack
        override fun connectSync(receiver: IDataReceiver) {
            configure(receiver.building.pos())
        }
        @SendDataPack
        override fun disconnectSync(receiver: IDataReceiver) {
            configure(receiver.building.pos())
        }

        override fun connectedReceiver(): Int? =
            if (receiversPos.isEmpty)
                null
            else
                receiversPos.first()

        override fun beforeDraw() {
            if (consValid() && isUnloading && isSending) {
                shrinkingAnimObj.spend(delta())
            }
        }

        override fun maxReceiverConnection() = maxConnection
        override fun acceptItem(source: Building, item: Item) = false
        override fun canMultipleConnect() = true
        override fun connectedReceivers(): OrderedSet<Int> = receiversPos
        override fun getBuilding() = this
        override fun getTile(): Tile = tile
        override fun getBlock() = this@SmartUnloader
        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            receiversPos = read.intSet()
            restored = true
        }

        override fun write(write: Writes) {
            super.write(write)
            write.intSet(receiversPos)
        }
    }

    @ClientOnly lateinit var UnloadingAni: AniStateU
    @ClientOnly lateinit var NoPowerAni: AniStateU
    @ClientOnly lateinit var BlockedAni: AniStateU
    override fun genAniConfig() {
        config {
            From(NoPowerAni) To UnloadingAni When {
                it.consValid()
            }

            From(UnloadingAni) To NoPowerAni When {
                !it.consValid()
            } To BlockedAni When {
                !it.isUnloading || !it.isSending
            }

            From(BlockedAni) To NoPowerAni When {
                !it.consValid()
            } To UnloadingAni When {
                it.isUnloading && it.isSending
            }
        }
    }

    override fun genAniState() {
        UnloadingAni = addAniState("Unloading") {
            it.shrinkingAnimObj.draw(it.x, it.y)
        }
        NoPowerAni = addAniState("NoPower") {
            DrawTR(NoPowerTR, it.x, it.y)
        }
        BlockedAni = addAniState("Blocked") {
            it.shrinkingAnimObj.draw(R.C.Stop, it.x, it.y)
        }
    }
}