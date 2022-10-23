package net.liplum.blocks.ddos

import arc.func.Prov
import arc.scene.ui.layout.Table
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.Time
import mindustry.Vars
import mindustry.content.Bullets
import mindustry.entities.bullet.BulletType
import mindustry.game.EventType
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.type.Item
import mindustry.world.blocks.defense.turrets.Turret
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.annotations.SubscribeEvent
import net.liplum.api.cyber.IDataReceiver
import net.liplum.api.cyber.IDataSender
import net.liplum.api.cyber.drawLinkedLineToReceiverWhenConfiguring
import net.liplum.api.cyber.req
import net.liplum.common.delegate.Delegate1
import plumy.core.Serialized
import plumy.core.ClientOnly
import net.liplum.render.postToastTextOn
import net.liplum.render.removeToastOn
import net.liplum.ui.ItemProgressImage
import plumy.dsl.AddBar
import net.liplum.utils.ItemTypeAmount
import net.liplum.utils.subBundle
import plumy.dsl.ID

class DDoS(name: String) : Turret(name) {
    @DebugOnly @ClientOnly var itemRow = 5
    @JvmField var acceptTime = 120f
    @JvmField var bulletType: BulletType = Bullets.placeholder
    @JvmField var maxDamage = 120f
    @JvmField var hitSizer: Bullet.() -> Float = { 4f }
    @JvmField var maxItemReachAttenuation = 20
    @JvmField var attenuation = 0.5f
    @JvmField var maxConnection = -1
    @JvmField var usedItemCooldownTimePreItem = 10f

    init {
        buildType = Prov { DDoSBuild() }
        consumeAmmoOnce = true
        hasItems = true
        hasPower = true
        itemCapacity = 80
    }

    override fun init() {
        attenuation = attenuation.coerceIn(0f, 1f)
        super.init()
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            AddBar<DDoSBuild>("requirement",
                { curAcceptItem.localizedName },
                { curAcceptItem.color },
                { 1f }
            )
            AddBar<DDoSBuild>("cur-index",
                { curIndex.toString() },
                { curAcceptItem.color },
                { curIndex.toFloat() / (alreadyUsed.size - 1) }
            )
        }
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        this.drawLinkedLineToReceiverWhenConfiguring(x, y)
    }

    inner class DDoSBuild : TurretBuild(),
        IDataReceiver {
        @Serialized
        var alreadyUsed: IntArray = IntArray(ItemTypeAmount())
        @Serialized
        var curIndex = 0
            set(value) {
                field = value % ItemTypeAmount()
            }
        @Serialized
        var acceptCounter = 0f
        @Serialized
        var usedItemCooldownCounter = 0f
        @Serialized
        var senders = OrderedSet<Int>()
        val curAcceptItem: Item
            get() = Vars.content.items()[curIndex]
        val curItemUsed: Int
            get() = alreadyUsed[curIndex]
        val usedItemCooldownTimeInMap: Float
            get() = usedItemCooldownTimePreItem * enabledItemsInMap.size

        override fun updateTile() {
            acceptCounter += Time.delta
            if (acceptCounter >= acceptTime) {
                acceptCounter -= acceptTime
                nextItem()
            }
            usedItemCooldownCounter += delta()
            if (usedItemCooldownCounter >= usedItemCooldownTimeInMap) {
                usedItemCooldownCounter -= usedItemCooldownTimeInMap
                coolDownUsedItems()
            }
            unit.ammo(unit.type().ammoCapacity * totalAmmo / maxAmmo.toFloat())
            super.updateTile()
        }

        fun coolDownUsedItems(number: Int = 1) {
            for (enabled in enabledItemsInMap) {
                val id = enabled.ID
                alreadyUsed[id] = (alreadyUsed[id] - number).coerceAtLeast(0)
            }
        }

        fun nextItem() {
            curIndex += 1 + skipTable[curIndex]
            curIndex %= alreadyUsed.size
            onRequirementUpdated(this)
        }

        fun peakNextItem(): Item {
            var index = curIndex + 1 + skipTable[curIndex]
            index %= alreadyUsed.size
            return Vars.content.items()[index]
        }

        override fun acceptItem(source: Building, item: Item): Boolean {
            return if (item == curAcceptItem) {
                removeToastOn(this)
                items[item] < getMaximumAccepted(item)
            } else {
                ClientOnly {
                    subBundle("deny").postToastTextOn(this, R.C.RedAlert, overwrite = false)
                }
                false
            }
        }

        override fun peekAmmo(): BulletType? {
            return if (items[curAcceptItem] > 0) bulletType else null
        }

        override fun hasAmmo(): Boolean {
            if (!canConsume()) return false
            return items[curAcceptItem] > 0
        }

        override fun useAmmo(): BulletType {
            if (cheating()) return bulletType
            items.remove(curAcceptItem, 1)
            return bulletType
        }

        override fun handleBullet(bullet: Bullet, offsetX: Float, offsetY: Float, angleOffset: Float) {
            bullet.fdata = curAcceptItem.id.toFloat()
            bullet.damage = maxDamage * (1f - attenuation * (curItemUsed.toFloat() / maxItemReachAttenuation).coerceAtMost(1f))
            bullet.hitSize = bullet.hitSizer()
        }

        override fun shoot(type: BulletType) {
            alreadyUsed[curIndex]++
            super.shoot(type)
        }

        override fun display(table: Table) {
            super.display(table)
            DebugOnly {
                table.row()
                table.add(Table().apply {
                    left()
                    for ((i, item) in enabledItemsInMap.withIndex()) {
                        add(ItemProgressImage(item.uiIcon) {
                            alreadyUsed[item.ID] / maxItemReachAttenuation.toFloat()
                        }.apply {
                            img.topDown = false
                            img.alpha = 0.7f
                        })
                        if ((i + 1) % itemRow == 0) {
                            row()
                        }
                    }
                })
            }
        }

        override fun receiveDataFrom(sender: IDataSender, item: Item, amount: Int) {
            if (this.isConnectedTo(sender)) {
                items.add(item, amount)
            }
        }

        override fun getAcceptedAmount(sender: IDataSender, item: Item): Int {
            if (!canConsume()) return 0
            return if (item == curAcceptItem) getMaximumAccepted(curAcceptItem) - items[curAcceptItem]
            else {
                ClientOnly {
                    subBundle("deny").postToastTextOn(this, R.C.RedAlert, overwrite = false)
                }
                0
            }
        }
        // TODO: Serialized
        override val requirements: Seq<Item>
            get() = curAcceptItem.req
        override val connectedSenders = senders
        override val onRequirementUpdated: Delegate1<IDataReceiver> = Delegate1()
        override val maxSenderConnection = maxConnection
    }

    companion object {
        val emptySkipTable = IntArray(0)
        var enabledItemsInMap = Seq<Item>(Vars.content.items().size)
        var skipTable = emptySkipTable
        fun updateEnabledItems() {
            enabledItemsInMap.set(Vars.content.items())
            enabledItemsInMap.removeAll { it.hidden || it in Vars.state.rules.hiddenBuildItems }
        }

        fun updateSkipTable() {
            run {
                val len = ItemTypeAmount()
                if (len == 0) {
                    skipTable = emptySkipTable
                    return
                }
                skipTable = IntArray(len)
                val flagTable = BooleanArray(len)
                for (hidden in Vars.state.rules.hiddenBuildItems) {
                    flagTable[hidden.ID] = true
                }
                for (disabled in Vars.content.items()) {
                    if (disabled.hidden) flagTable[disabled.ID] = true
                }
                var counter = 0
                for (i in flagTable.size - 1 downTo 0) {
                    val isHidden = flagTable[i]
                    if (isHidden) {
                        counter++
                        skipTable[i] = counter
                    } else
                        counter = 0
                }
                val first = skipTable.first()
                System.arraycopy(skipTable, 1, skipTable, 0, skipTable.size - 1)
                skipTable[skipTable.size - 1] = first
            }
            run {
                val last = skipTable.last()
                if (last != 0) {
                    for ((curIndex, reversedIndex) in (skipTable.size - 2 downTo 0).withIndex()) {
                        val count = skipTable[reversedIndex]
                        if (count != 0)
                            skipTable[reversedIndex] = curIndex + 1 + last
                        else
                            break
                    }
                }
            }
        }
        @SubscribeEvent(EventType.WorldLoadEvent::class)
        fun updateItemInfoInCurrentMap() {
            updateEnabledItems()
            updateSkipTable()
        }
    }
}