package net.liplum.blocks.cloud

import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.struct.ObjectSet
import arc.struct.OrderedSet
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.gen.Teamc
import mindustry.type.Item
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.power.PowerBlock
import mindustry.world.meta.BlockGroup
import mindustry.world.modules.ItemModule
import net.liplum.*
import net.liplum.animations.anims.Animation
import net.liplum.animations.anims.IFrameIndexer
import net.liplum.animations.anims.ixAuto
import net.liplum.animations.anis.AniConfig
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.config
import net.liplum.animations.blocks.*
import net.liplum.api.data.CyberU
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender
import net.liplum.api.data.dr
import net.liplum.delegates.Delegate1
import net.liplum.persistance.intSet
import net.liplum.utils.*

private typealias Anim = Animation
private typealias BGType = BlockGroupType<Cloud, Cloud.CloudBuild>
private typealias BType = BlockType<Cloud, Cloud.CloudBuild>
private typealias Ani = AniState<Cloud, Cloud.CloudBuild>

open class Cloud(name: String) : PowerBlock(name) {
    @ClientOnly lateinit var cloud: TR
    @ClientOnly lateinit var FloatingCloudAnim: Anim
    @ClientOnly lateinit var DataTransferAnim: Anim
    @ClientOnly lateinit var ShredderAnim: Anim
    @ClientOnly lateinit var BlockG: BGType
    @ClientOnly lateinit var CloudAniBlock: BType
    @ClientOnly lateinit var DataAniBlock: BType
    @ClientOnly lateinit var ShredderAniBlock: BType
    @ClientOnly lateinit var CloudAniConfig: AniConfig<Cloud, CloudBuild>
    @ClientOnly lateinit var CloudIdleAni: Ani
    @ClientOnly lateinit var CloudNoPowerAni: Ani
    var cloudFloatRange = 1f

    init {
        solid = true
        update = true
        hasItems = true
        saveConfig = true
        configurable = true
        itemCapacity = 10
        noUpdateDisabled = true
        unloadable = false
        group = BlockGroup.none
        allowConfigInventory = false

        ClientOnly {
            this.genAnimState()
            this.genAniConfig()
            this.genBlockTypes()
        }

        config(Integer::class.java) { obj: CloudBuild, receiverPackedPos ->
            obj.setReceiver(receiverPackedPos.toInt())
        }
        configClear { obj: CloudBuild ->
            obj.clearReceiver()
        }
    }

    override fun setBars() {
        super.setBars()
        bars.remove("items")
        DebugOnly {
            bars.addSenderInfo<CloudBuild>()
            bars.addReceiverInfo<CloudBuild>()
            bars.addTeamInfo()
        }
    }

    override fun load() {
        super.load()
        cloud = this.sub("cloud")
        DataTransferAnim = this.autoAnim("data-transfer", 18, 50f)
        ShredderAnim = this.autoAnim("shredder", 13, 60f)
    }

    override fun outputsItems() = false
    open inner class CloudBuild : Building(), IShared, IDataReceiver, IDataSender {
        lateinit var cloudRoom: SharedRoom
        lateinit var aniBlockGroupObj: BlockGroupObj<Cloud, CloudBuild>
        lateinit var info: CloudInfo
        lateinit var floatingCloudIx: IFrameIndexer
        lateinit var dataTransferIx: IFrameIndexer
        lateinit var shredderIx: IFrameIndexer
        var cloudXOffset = 0f
        var cloudYOffset = 0f
        var teamID = 0
        val isWorking: Boolean
            get() = !power.status.isZero()
        @JvmField var onRequirementUpdated: Delegate1<IDataReceiver> = Delegate1()
        override fun getOnRequirementUpdated() = onRequirementUpdated
        override fun updateTile() {
        }
        @CalledBySync
        open fun setReceiver(pos: Int) {
            if (pos in info.receiversPos) {
                pos.dr()?.let { disconnectReceiver(it) }
            } else {
                pos.dr()?.let { connectReceiver(it) }
            }
        }
        @CalledBySync
        open fun clearReceiver() {
            info.receiversPos.clear()
        }
        @CalledBySync
        open fun connectReceiver(receiver: IDataReceiver) {
            info.receiversPos.add(receiver.building.pos())
        }
        @CalledBySync
        open fun disconnectReceiver(receiver: IDataReceiver) {
            info.receiversPos.remove(receiver.building.pos())
        }

        override fun created() {
            cloudRoom = LiplumCloud.getCloud(team)
            cloudRoom.online(this)
            ClientOnly {
                //floatingCloudIx = floatingCloudAnim.indexByTimeScale(this)
                dataTransferIx = DataTransferAnim.ixAuto(this)
                shredderIx = ShredderAnim.ixAuto(this)
                aniBlockGroupObj = BlockG.newObj(this@Cloud, this)
            }
        }

        override fun onRemoved() {
            cloudRoom.offline(this)
        }

        override fun sendData(receiver: IDataReceiver, item: Item, amount: Int): Int {
            val rest = super.sendData(receiver, item, amount)
            info.lastReceiveOrSendDataTime = 0f
            return rest
        }

        override fun receiveData(sender: IDataSender, item: Item, amount: Int) {
            val space = itemCapacity - items[item]//100-98=2 or 100-87=13
            if (space >= 0) {//2>0 or 13>0
                val rest = amount - space//5-2=3 or 5-13=-8
                if (rest >= 0) {
                    items.add(item, space)//space < amount
                    info.lastShredTime = 0f
                } else {
                    items.add(item, amount)//space > amount
                }
                info.lastReceiveOrSendDataTime = 0f
            } else {
                items[item] = itemCapacity
                info.lastShredTime = 0f
            }
        }

        override fun acceptedAmount(sender: IDataSender, itme: Item): Int {
            return -1
        }

        override fun acceptItem(source: Building, item: Item) = false
        override fun handleItem(source: Building, item: Item) {
        }

        override fun handleStack(item: Item, amount: Int, source: Teamc) {
        }
        @ClientOnly
        override fun canAcceptAnyData(sender: IDataSender) = true
        override fun getRequirements(): Array<Item>? = null
        @ClientOnly
        override fun isBlocked() = false
        @CalledBySync
        override fun connect(sender: IDataSender) {
            info.sendersPos.add(sender.building.pos())
        }
        @CalledBySync
        override fun disconnect(sender: IDataSender) {
            info.sendersPos.remove(sender.building.pos())
        }
        @SendDataPack
        override fun connectSync(receiver: IDataReceiver) {
            configure(receiver.building.pos())
        }
        @SendDataPack
        override fun disconnectSync(receiver: IDataReceiver) {
            configure(receiver.building.pos())
        }

        override fun draw() {
            WhenRefresh {
                aniBlockGroupObj.update()
            }
            super.draw()
            aniBlockGroupObj.drawBuilding()
        }

        override fun onConfigureTileTapped(other: Building): Boolean {
            if (this === other) {
                deselect()
                configure(null)
                return false
            }
            val pos = other.pos()
            if (pos in info.receiversPos) {
                deselect()
                pos.dr()?.let { disconnectSync(it) }
                return false
            }
            if (other is IDataReceiver) {
                deselect()
                if (other.acceptConnection(this)) {
                    connectSync(other)
                }
                return false
            }
            return true
        }

        override fun drawSelect() {
            drawDataNetGraphic()
        }

        override fun drawConfigure() {
            super.drawConfigure()
            drawDataNetGraphic()
        }

        open fun drawDataNetGraphic() {
            G.init()
            G.drawSurroundingCircle(tile, R.C.Cloud)

            CyberU.drawSenders(this, info.sendersPos)
            CyberU.drawReceivers(this, info.receiversPos)
        }

        override fun connectedSenders(): ObjectSet<Int> =
            info.sendersPos

        override fun connectedSender(): Int? =
            if (info.sendersPos.isEmpty)
                null
            else
                info.sendersPos.first()

        override fun canMultipleConnect() = true
        override fun connectedReceiver(): Int? =
            if (info.receiversPos.isEmpty)
                null
            else
                info.receiversPos.first()

        override fun connectedReceivers(): OrderedSet<Int> =
            info.sendersPos

        override fun acceptConnection(sender: IDataSender): Boolean {
            return true
        }

        override fun write(write: Writes) {
            super.write(write)
            write.intSet(info.sendersPos)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            info.sendersPos = read.intSet()
        }

        override fun getSharedItems(): ItemModule = items
        override fun setSharedItems(itemModule: ItemModule) {
            items = itemModule
        }

        override fun getBuilding() = this
        override fun getSharedInfo(): CloudInfo = info
        override fun setSharedInfo(info: CloudInfo) {
            this.info = info
        }

        override fun getTile(): Tile = tile
        override fun getBlock(): Block = block
        override fun maxSenderConnection() = -1
        override fun maxReceiverConnection() = -1
    }

    open fun genAnimState() {
        CloudIdleAni = AniState("Idle") {
            Draw.rect(
                cloud,
                it.x + it.cloudXOffset,
                it.y + it.cloudYOffset
            )
        }
        CloudNoPowerAni = AniState("NoPower")
    }

    open fun genAniConfig() {
        CloudAniConfig = config {
            From(CloudNoPowerAni) To CloudIdleAni When {
                it.isWorking
            }
            From(CloudIdleAni) To CloudNoPowerAni When {
                !it.isWorking
            }
        }
    }

    open fun genBlockTypes() {
        BlockG = BlockGroupType {
            CloudAniBlock = addType(BlockType.byObj(ShareMode.UseMain) { block, build ->
                object : BlockObj<Cloud, CloudBuild>(block, build, CloudAniBlock) {
                    var cloudAniSM = CloudAniConfig.gen(block, build)
                    override fun update() {
                        cloudAniSM.update()
                    }

                    override fun drawBuild() {
                        xOffset = Mathf.range(cloudFloatRange)
                        build.cloudXOffset = xOffset
                        yOffset = Mathf.range(cloudFloatRange)
                        build.cloudYOffset = yOffset
                        cloudAniSM.drawBuilding()
                    }
                }
            })

            DataAniBlock = addType(BlockType.render { _, build ->
                if (build.isWorking && build.info.isDataTransferring) {
                    DataTransferAnim.draw(build.dataTransferIx) {
                        Draw.rect(
                            it,
                            build.x + xOffset,
                            build.y + yOffset
                        )
                    }
                }
            })

            ShredderAniBlock = addType(BlockType.render { _, build ->
                if (build.isWorking && build.info.isShredding) {
                    ShredderAnim.draw(build.shredderIx) {
                        Draw.rect(
                            it,
                            build.x + xOffset,
                            build.y + yOffset,
                        )
                    }
                }
            })
        }
    }
}