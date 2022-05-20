package net.liplum.blocks.cloud

import arc.graphics.g2d.Draw
import arc.struct.ObjectSet
import arc.struct.OrderedSet
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.gen.Teamc
import mindustry.type.Item
import mindustry.world.blocks.power.PowerBlock
import mindustry.world.meta.BlockGroup
import mindustry.world.modules.ItemModule
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.WhenRefresh
import net.liplum.api.cyber.*
import net.liplum.lib.TR
import net.liplum.lib.delegates.Delegate1
import net.liplum.lib.persistance.intSet
import net.liplum.mdt.*
import net.liplum.mdt.animations.anims.Animation
import net.liplum.mdt.animations.anims.IFrameIndexer
import net.liplum.mdt.animations.anims.ixAuto
import net.liplum.mdt.animations.anis.AniConfig
import net.liplum.mdt.animations.anis.AniState
import net.liplum.mdt.animations.anis.config
import net.liplum.mdt.animations.blocks.*
import net.liplum.mdt.render.G
import net.liplum.mdt.render.postToastTextOn
import net.liplum.mdt.ui.bars.removeItemsInBar
import net.liplum.mdt.utils.autoAnim
import net.liplum.mdt.utils.inMod
import net.liplum.mdt.utils.sub
import net.liplum.mdt.utils.subBundle
import net.liplum.utils.addReceiverInfo
import net.liplum.utils.addSenderInfo
import net.liplum.utils.addTeamInfo

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
    @ClientOnly lateinit var NoPowerTR: TR
    @JvmField var maxConnection = -1

    init {
        solid = true
        update = true
        hasItems = true
        saveConfig = true
        configurable = true
        itemCapacity = 10
        noUpdateDisabled = true
        unloadable = false
        canOverdrive = false
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
            obj.clearReceivers()
        }
    }

    override fun setBars() {
        super.setBars()
        removeItemsInBar()
        DebugOnly {
            addSenderInfo<CloudBuild>()
            addReceiverInfo<CloudBuild>()
            addTeamInfo()
        }
    }

    override fun load() {
        super.load()
        cloud = this.sub("cloud")
        DataTransferAnim = this.autoAnim("data-transfer", 18, 50f)
        ShredderAnim = this.autoAnim("shredder", 13, 60f)
        NoPowerTR = this.inMod("rs-no-power")
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        this.drawLinkedLineToReceiverWhenConfiguring(x, y)
    }

    override fun outputsItems() = false
    open inner class CloudBuild : Building(), IShared,
        IDataReceiver,
        IDataSender {
        lateinit var cloudRoom: SharedRoom
        lateinit var aniBlockGroupObj: BlockGroupObj<Cloud, CloudBuild>
        lateinit var info: CloudInfo
        lateinit var floatingCloudIx: IFrameIndexer
        lateinit var dataTransferIx: IFrameIndexer
        lateinit var shredderIx: IFrameIndexer
        var teamID = 0
        @JvmField var onRequirementUpdated: Delegate1<IDataReceiver> = Delegate1()
        override fun getOnRequirementUpdated() = onRequirementUpdated

        init {
            ClientOnly {
                //floatingCloudIx = floatingCloudAnim.indexByTimeScale(this)
                dataTransferIx = DataTransferAnim.ixAuto(this)
                shredderIx = ShredderAnim.ixAuto(this)
                aniBlockGroupObj = BlockG.newObj(this@Cloud, this)
            }
        }

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
        open fun clearReceivers() {
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
        }

        override fun onRemoved() {
            cloudRoom.offline(this)
            onRequirementUpdated.clear()
        }

        override fun sendData(receiver: IDataReceiver, item: Item, amount: Int): Int {
            val rest = super.sendData(receiver, item, amount)
            info.lastReceiveOrSendDataTime = 0f
            return rest
        }

        override fun receiveData(sender: IDataSender, item: Item, amount: Int) {
            if (!this.isConnectedWith(sender)) return
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

        override fun acceptedAmount(sender: IDataSender, itme: Item): Int = -1
        override fun acceptItem(source: Building, item: Item) = false
        override fun handleItem(source: Building, item: Item) {
        }

        override fun handleStack(item: Item, amount: Int, source: Teamc) {
        }

        override fun getRequirements(): Array<Item>? = null
        @ClientOnly
        override fun isBlocked() = false
        @SendDataPack
        override fun connectSync(receiver: IDataReceiver) {
            val pos = receiver.building.pos()
            if (pos !in info.receiversPos) {
                configure(pos)
            }
        }
        @SendDataPack
        override fun disconnectSync(receiver: IDataReceiver) {
            val pos = receiver.building.pos()
            if (pos in info.receiversPos) {
                configure(pos)
            }
        }

        override fun draw() {
            WhenNotPaused {
                aniBlockGroupObj.spend(delta())
            }
            WhenRefresh {
                aniBlockGroupObj.update()
            }
            Draw.rect(region, x, y)
            aniBlockGroupObj.drawBuilding()
            drawTeamTop()
        }

        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            val pos = other.pos()
            if (pos in info.receiversPos) {
                if (!canMultipleConnect()) {
                    deselect()
                }
                pos.dr()?.let { disconnectSync(it) }
                return false
            }
            if (other is IDataReceiver) {
                if (other is CloudBuild) {
                    subBundle("self-connect").postToastTextOn(other, R.C.RedAlert)
                    return false
                }
                if (!canMultipleConnect()) {
                    deselect()
                }
                if (other.acceptConnection(this)) {
                    connectSync(other)
                }
                return false
            }
            return true
        }

        override fun drawSelect() {
            whenNotConfiguringSender {
                drawDataNetGraphic()
            }
        }

        override fun drawConfigure() {
            super.drawConfigure()
            drawDataNetGraphic()
        }

        open fun drawDataNetGraphic() {
            G.drawSurroundingCircle(tile, R.C.Cloud)

            this.drawSenders(info.sendersPos)
            this.drawReceivers(info.receiversPos)
        }

        override fun getConnectedSenders(): ObjectSet<Int> =
            info.sendersPos

        override fun getConnectedReceiver(): Int? =
            if (info.receiversPos.isEmpty)
                null
            else
                info.receiversPos.first()

        override fun getConnectedReceivers(): OrderedSet<Int> =
            info.sendersPos

        override fun acceptConnection(sender: IDataSender): Boolean = true
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

        override fun getSharedInfo(): CloudInfo = info
        override fun setSharedInfo(info: CloudInfo) {
            this.info = info
        }

        override fun maxSenderConnection() = maxConnection
        override fun maxReceiverConnection() = maxConnection
    }

    open fun genAnimState() {
        CloudIdleAni = AniState("Idle") {
            cloud.Draw(x, y)
        }
        CloudNoPowerAni = AniState("NoPower") {
            NoPowerTR.Draw(x, y)
        }
    }

    open fun genAniConfig() {
        CloudAniConfig = config {
            From(CloudNoPowerAni) To CloudIdleAni When {
                canConsume()
            }
            From(CloudIdleAni) To CloudNoPowerAni When {
                !canConsume()
            }
            transitionDuration = 60f
        }
    }

    inner class CloudBlockObj(
        block: Cloud, build: CloudBuild
    ) : BlockObj<Cloud, CloudBuild>(block, build, CloudAniBlock) {
        var cloudAniSM = CloudAniConfig.gen(block, build)
        override fun update() {
            cloudAniSM.update()
        }

        override fun spend(time: Float) {
            cloudAniSM.spend(time)
        }

        override fun drawBuild() {
            xOffset = 0f
            yOffset = 0f
            cloudAniSM.drawBuilding()
        }
    }

    open fun genBlockTypes() {
        BlockG = BlockGroupType {
            CloudAniBlock = addType(BlockType.byObj(ShareMode.UseMain) { block, build ->
                CloudBlockObj(block, build)
            })

            DataAniBlock = addType(BlockType.render { _, build ->
                if (build.canConsume() && build.info.isDataTransferring) {
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
                if (build.canConsume() && build.info.isShredding) {
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