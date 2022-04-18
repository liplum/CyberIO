package net.liplum.blocks.gadgets

import arc.math.Mathf
import arc.scene.ui.Label
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.logic.LAccess
import mindustry.type.Item
import mindustry.ui.Bar
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import net.liplum.*
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.animations.anims.AnimationObj
import net.liplum.lib.animations.anis.AniState
import net.liplum.lib.Draw
import net.liplum.lib.animations.anis.config
import net.liplum.lib.ui.bars.removeItems
import net.liplum.persistance.intSet
import net.liplum.utils.*
import kotlin.math.absoluteValue
import kotlin.math.log2

private typealias AniStateU = AniState<SmartUnloader, SmartUnloader.SmartULDBuild>
private typealias SmartDIS = SmartDistributor.SmartDISBuild

open class SmartUnloader(name: String) : AniedBlock<SmartUnloader, SmartUnloader.SmartULDBuild>(name) {
    /**
     * The lager the number the slower the unloading speed. Belongs to [0,+inf)
     */
    @JvmField var unloadSpeed = 1f
    @JvmField var maxConnection = 5
    @JvmField @ClientOnly var SendingTime = 60f
    @JvmField @ClientOnly var UnloadTime = 60f
    @ClientOnly lateinit var ShrinkingAnim: Animation
    @JvmField @ClientOnly var ShrinkingAnimFrames = 13
    @JvmField @ClientOnly var ShrinkingAnimDuration = 120f
    @ClientOnly lateinit var CoverTR: TR
    @ClientOnly lateinit var NoPowerTR: TR
    @JvmField var powerUsagePerItem = 2.5f
    @JvmField var powerUsagePerConnection = 2f
    @JvmField var powerUsageBasic = 1.5f
    @JvmField var boost2Count: (Float) -> Int = {
        if (it <= 1.1f)
            1
        else if (it in 1.1f..2.1f)
            2
        else if (it in 2.1f..3f)
            3
        else
            Mathf.round(log2(it + 5.1f))
    }

    init {
        solid = true
        update = true
        hasPower = true
        hasItems = true
        group = BlockGroup.transportation
        noUpdateDisabled = true
        unloadable = false
        canOverdrive = true
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

    override fun init() {
        consumes.powerDynamic<SmartULDBuild> {
            (powerUsageBasic
                    + powerUsagePerItem * it.needUnloadItems.size
                    + powerUsagePerConnection * it.getConnectedReceivers().size)
        }
        super.init()
    }

    override fun load() {
        super.load()
        NoPowerTR = this.inMod("rs-no-power")
        ShrinkingAnim = this.autoAnim("shrink", ShrinkingAnimFrames, ShrinkingAnimDuration)
        CoverTR = this.sub("cover")
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.powerUse)
        stats.add(Stat.powerUse) {
            val l = Label("$contentType.$name.stats.power-use".bundle)
            it.add(l)
        }
    }

    override fun setBars() {
        super.setBars()
        UndebugOnly {
            bars.removeItems()
        }
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
        var receivers = OrderedSet<Int>()
        var nearby: Seq<Building> = Seq()
        var trackers: Array<Tracker> = Array(ItemTypeAmount()) {
            Tracker(maxConnection)
        }
        var needUnloadItems: OrderedSet<Item> = OrderedSet(ItemTypeAmount())
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
        var justRestored = false

        init {
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
            receivers.removeAll { !it.dr().exists }
        }

        override fun updateTile() {
            if (justRestored) {
                updateTracker()
                justRestored = false
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
            if (receivers.isEmpty) {
                return
            }
            unloadTimer += delta()
            if (unloadTimer >= unloadSpeed) {
                unloadTimer = 0f
                val unloaded = DoMultipleBool(canOverdrive, boost2Count(timeScale), this::unload)
                ClientOnly {
                    if (unloaded) {
                        lastUnloadTime = 0f
                    }
                }
            }
            val sent = DoMultipleBool(canOverdrive, boost2Count(timeScale), this::sendData)
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
                    val containerItems = container.items ?: continue
                    if (!item.canBeUnloadedToThis()) {
                        continue
                    }
                    if (containerItems.has(item)) {
                        containerItems.remove(item, 1)
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
            needUnloadItems.clear()
            for (receiverPos in receivers) {
                val receiver = receiverPos.dr()
                if (receiver != null) {
                    val reqs = receiver.requirements
                    if (reqs != null) {
                        for (req in reqs) {
                            val tracker = trackers[req!!.ID]
                            if (tracker.canAddMore()) {
                                needUnloadItems.add(req)
                                tracker.add(receiver)
                            }
                        }
                    }
                }
            }
            DebugOnly {
                needUnloadItemsText = genNeedUnloadItemsText()
            }
        }

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            updateUnloaded()
            updateTracker()
        }
        @CioDebugOnly
        var needUnloadItemsText: String = ""
        @CioDebugOnly
        fun genNeedUnloadItemsText() = needUnloadItems.genText()
        override fun drawSelect() {
            this.drawDataNetGraphic()
            DebugOnly {
                if (needUnloadItemsText.isNotEmpty()) {
                    drawPlaceText(needUnloadItemsText, tileX(), tileY(), true)
                }
            }
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
            if (pos in receivers) {
                if (!canMultipleConnect()) {
                    deselect()
                }
                pos.dr()?.let { disconnectSync(it) }
                return false
            }
            if (other is IDataReceiver) {
                if (!canMultipleConnect()) {
                    deselect()
                }
                if (canHaveMoreReceiverConnection() &&
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
            if (pos in receivers) {
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

        override fun onProximityAdded() {
            super.onProximityAdded()
            resubscribeRequirementUpdated()
        }

        open fun resubscribeRequirementUpdated() {
            receivers.forEach { pos ->
                pos.dr()?.let {
                    it.onRequirementUpdated += ::onReceiverRequirementsUpdated
                }
            }
        }
        @CalledBySync
        open fun connectReceiver(receiver: IDataReceiver) {
            if (receivers.add(receiver.building.pos())) {
                updateTracker()
                receiver.onRequirementUpdated += ::onReceiverRequirementsUpdated
            }
        }
        @CalledBySync
        open fun disconnectReceiver(receiver: IDataReceiver) {
            if (receivers.remove(receiver.building.pos())) {
                updateTracker()
                receiver.onRequirementUpdated -= ::onReceiverRequirementsUpdated
            }
        }
        @CalledBySync
        open fun clearReceivers() {
            receivers.forEach { pos ->
                pos.dr()?.let {
                    it.disconnect(this)
                    it.onRequirementUpdated -= ::onReceiverRequirementsUpdated
                }
            }
            receivers.clear()
            updateTracker()
        }
        @SendDataPack
        override fun connectSync(receiver: IDataReceiver) {
            val pos = receiver.building.pos()
            if (pos !in receivers) {
                configure(pos)
            }
        }
        @SendDataPack
        override fun disconnectSync(receiver: IDataReceiver) {
            val pos = receiver.building.pos()
            if (pos in receivers) {
                configure(pos)
            }
        }

        override fun getConnectedReceiver(): Int? =
            if (receivers.isEmpty)
                null
            else
                receivers.first()

        override fun beforeDraw() {
            if (consValid() && isUnloading && isSending) {
                shrinkingAnimObj.spend(delta())
            }
        }

        override fun maxReceiverConnection() = maxConnection
        override fun acceptItem(source: Building, item: Item) = false
        override fun getConnectedReceivers(): OrderedSet<Int> = receivers
        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            receivers = read.intSet()
            justRestored = true
        }

        override fun write(write: Writes) {
            super.write(write)
            write.intSet(receivers)
        }

        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot ->
                    if (p1 is IDataReceiver) connectSync(p1)
                else -> super.control(type, p1, p2, p3, p4)
            }
        }

        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot -> {
                    val receiver = buildAt(p1, p2)
                    if (receiver is IDataReceiver) connectSync(receiver)
                }
                else -> super.control(type, p1, p2, p3, p4)
            }
        }
    }

    @ClientOnly lateinit var UnloadingAni: AniStateU
    @ClientOnly lateinit var NoPowerAni: AniStateU
    @ClientOnly lateinit var BlockedAni: AniStateU
    override fun genAniConfig() {
        config {
            From(NoPowerAni) To UnloadingAni When {
                consValid()
            }

            From(UnloadingAni) To NoPowerAni When {
                !consValid()
            } To BlockedAni When {
                !isUnloading || !isSending
            }

            From(BlockedAni) To NoPowerAni When {
                !consValid()
            } To UnloadingAni When {
                isUnloading && isSending
            }
        }
    }

    override fun genAniState() {
        UnloadingAni = addAniState("Unloading") {
            shrinkingAnimObj.draw(x, y)
        }
        NoPowerAni = addAniState("NoPower") {
            NoPowerTR.Draw(x, y)
        }
        BlockedAni = addAniState("Blocked") {
            shrinkingAnimObj.draw(R.C.Stop, x, y)
        }
    }
}