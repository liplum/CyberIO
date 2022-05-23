package net.liplum.blocks.ddos

import arc.scene.ui.layout.Table
import arc.struct.OrderedSet
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
import net.liplum.api.cyber.req
import net.liplum.lib.delegates.Delegate1
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.render.postToastTextOn
import net.liplum.mdt.render.removeToastOn
import net.liplum.mdt.ui.DynamicItemImage
import net.liplum.mdt.ui.bars.AddBar
import net.liplum.mdt.utils.ID
import net.liplum.mdt.utils.ItemTypeAmount
import net.liplum.mdt.utils.subBundle

class DDoS(name: String) : Turret(name) {
    @DebugOnly @ClientOnly var itemRow = 5
    var acceptTime = 120f
    var bulletType: BulletType = Bullets.placeholder
    var maxDamage = 120f
    var maxItemReachAttenuation = 20
    var attenuation = 0.5f
    var maxConnection = -1

    init {
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

    inner class DDoSBuild : TurretBuild(),
        IDataReceiver {
        var alreadyUsed: IntArray = IntArray(ItemTypeAmount())
        var curIndex = 0
            set(value) {
                field = value % ItemTypeAmount()
            }
        var acceptCounter = 0f
        val curAcceptItem: Item
            get() = Vars.content.items()[curIndex]
        val curItemUsed: Int
            get() = alreadyUsed[curIndex]
        var senders = OrderedSet<Int>()
        override fun updateTile() {
            acceptCounter += Time.delta
            if (acceptCounter > acceptTime) {
                acceptCounter -= acceptTime
                nextItem()
            }
            super.updateTile()
        }

        fun nextItem() {
            curIndex += 1 + skipTable[curIndex]
            curIndex %= alreadyUsed.size
            onRequirementUpdated(this)
        }

        override fun cheating() = true
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

        override fun peekAmmo(): BulletType {
            return bulletType
        }

        override fun hasAmmo(): Boolean {
            return true
        }

        override fun handleBullet(bullet: Bullet, offsetX: Float, offsetY: Float, angleOffset: Float) {
            bullet.fdata = curAcceptItem.id.toFloat()
            bullet.damage = maxDamage * (1f - attenuation * (curItemUsed.toFloat() / maxItemReachAttenuation).coerceAtMost(1f))
        }

        override fun shoot(type: BulletType) {
            alreadyUsed[curIndex]++
            super.shoot(type)
        }

        override fun display(table: Table) {
            super.display(table)
            table.row()
            table.add(Table().apply {
                left()
                for ((i, item) in Vars.content.items().withIndex()) {
                    add(DynamicItemImage(item.uiIcon) {
                        alreadyUsed[i]
                    })
                    if ((i + 1) % itemRow == 0) {
                        row()
                    }
                }
            })
        }

        override fun receiveData(sender: IDataSender, item: Item, amount: Int) {
            if (this.isConnectedWith(sender)) {
                items.add(item, amount)
            }
        }

        override fun acceptedAmount(sender: IDataSender, item: Item): Int {
            if (!canConsume()) return 0
            return if (item == curAcceptItem) getMaximumAccepted(curAcceptItem) - items[curAcceptItem]
            else {
                ClientOnly {
                    subBundle("deny").postToastTextOn(this, R.C.RedAlert, overwrite = false)
                }
                0
            }
        }

        override fun getRequirements(): Array<Item> = curAcceptItem.req
        @ClientOnly
        override fun isBlocked() = true
        override fun getConnectedSenders() = senders
        @JvmField var onRequirementUpdated: Delegate1<IDataReceiver> = Delegate1()
        override fun getOnRequirementUpdated() = onRequirementUpdated
        override fun maxSenderConnection() = maxConnection
    }

    companion object {
        val emptySkipTable = IntArray(0)
        var skipTable = emptySkipTable
        @SubscribeEvent(EventType.WorldLoadEvent::class)
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
    }
}