package net.liplum.blocks.cloud

import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.struct.ObjectSet
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
import net.liplum.animations.anims.blocks.AutoAnimationT
import net.liplum.animations.anis.AniConfig
import net.liplum.animations.anis.AniState
import net.liplum.animations.blocks.*
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender
import net.liplum.utils.*

private typealias Anim = AutoAnimationT<Cloud.CloudBuild>
private typealias AnimObj = AutoAnimationT<Cloud.CloudBuild>.Obj
private typealias BGType = BlockGroupType<Cloud, Cloud.CloudBuild>
private typealias BType = BlockType<Cloud, Cloud.CloudBuild>
private typealias Ani = AniState<Cloud, Cloud.CloudBuild>

open class Cloud(name: String) : PowerBlock(name) {
    lateinit var cloud: TR
    lateinit var floatingCloudAnim: Anim
    lateinit var dataTransferAnim: Anim
    lateinit var shredderAnim: Anim
    lateinit var blockGroup: BGType
    lateinit var CloudAniBlock: BType
    lateinit var DataAniBlock: BType
    lateinit var ShredderAniBlock: BType
    lateinit var CloudAniConfig: AniConfig<Cloud, CloudBuild>
    lateinit var CloudIdleAni: Ani
    lateinit var CloudNoPowerAni: Ani
    var cloudFloatRange = 1f

    init {
        solid = true
        update = true
        hasItems = true
        saveConfig = true
        configurable = true
        itemCapacity = 10

        group = BlockGroup.logic
        WhenCanAniStateLoad {
            this.genAnimState()
            this.genAniConfig()
            this.genBlockTypes()
        }
    }

    open fun genAnimState() {
        CloudIdleAni = AniState("Idle") { _, build ->
            Draw.rect(
                cloud,
                build.x + build.cloudXOffset,
                build.y + build.cloudYOffset
            )
        }
        CloudNoPowerAni = AniState("NoPower")
    }

    open fun genAniConfig() {
        CloudAniConfig = AniConfig()
        CloudAniConfig.defaultState(CloudNoPowerAni)
        CloudAniConfig.entry(CloudNoPowerAni, CloudIdleAni) { _, build -> build.isWorking }
        CloudAniConfig.entry(CloudIdleAni, CloudNoPowerAni) { _, build -> !build.isWorking }
        CloudAniConfig.build()
    }

    open fun genBlockTypes() {
        CloudAniBlock = object : BlockType<Cloud, CloudBuild>() {
            override fun newObj(block: Cloud, build: CloudBuild): BlockObj<Cloud, CloudBuild> = object :
                BlockObj<Cloud, CloudBuild>(block, build, this) {
                var cloudAniSM = CloudAniConfig.gen(block, build)
                override fun update() {
                    cloudAniSM.update()
                }

                override fun drawBuilding() {
                    xOffset = MathU.randomNP(cloudFloatRange)
                    build.cloudXOffset = xOffset
                    yOffset = MathU.randomNP(cloudFloatRange)
                    build.cloudYOffset = yOffset
                    cloudAniSM.drawBuilding()
                }
            }
        }

        DataAniBlock = object : BlockType<Cloud, CloudBuild>(ShareMode.KeepSelf) {
            override fun newObj(block: Cloud, build: CloudBuild): BlockObj<Cloud, CloudBuild> = object :
                BlockObj<Cloud, CloudBuild>(block, build, this) {
                override fun drawBuilding() {
                    if (build.isWorking && build.info.isDataTransferring) {
                        dataTransferAnim.draw(
                            build.dataTransferAnim,
                            build.x + xOffset,
                            build.y + yOffset
                        )
                    }
                }
            }
        }

        ShredderAniBlock = object : BlockType<Cloud, CloudBuild>(ShareMode.KeepSelf) {
            override fun newObj(block: Cloud, build: CloudBuild): BlockObj<Cloud, CloudBuild> = object :
                BlockObj<Cloud, CloudBuild>(block, build, this) {
                override fun drawBuilding() {
                    if (build.isWorking && build.info.isShredding) {
                        shredderAnim.draw(
                            build.shredderAnim,
                            build.x + xOffset,
                            build.y + yOffset,
                        )
                    }
                }
            }
        }
        blockGroup = BlockGroupType(CloudAniBlock, DataAniBlock, ShredderAniBlock)
    }

    override fun setBars() {
        super.setBars()
        bars.remove("items")
        DebugOnly {
            bars.addTeamInfo()
        }
    }

    override fun load() {
        super.load()
        cloud = this.subA("cloud")
        dataTransferAnim = this.autoAnimT("data-transfer", 18, 50f)
        shredderAnim = this.autoAnimT("shredder", 13, 60f)
    }

    override fun outputsItems() = false
    open inner class CloudBuild : Building(), IShared, IDataReceiver, IDataSender {
        lateinit var cloudRoom: SharedRoom
        lateinit var floatingCloudAnim: AnimObj
        lateinit var dataTransferAnim: AnimObj
        lateinit var shredderAnim: AnimObj
        lateinit var aniBlockGroupObj: BlockGroupObj<Cloud, CloudBuild>
        lateinit var info: CloudInfo
        var cloudXOffset = 0f
        var cloudYOffset = 0f
        var teamID = 0
        val isWorking: Boolean
            get() = !Mathf.zero(power.status)

        override fun updateTile() {
            info.lastReceiveOrSendDataTime += edelta()
            info.lastShredTime += edelta()
        }

        override fun created() {
            cloudRoom = LiplumCloud.getCloud(team)
            cloudRoom.online(this)
            if (CioMod.CanAniStateLoad) {
                //floatingCloudAnim = this@Cloud.floatingCloudAnim.gen()
                dataTransferAnim = this@Cloud.dataTransferAnim.gen()
                shredderAnim = this@Cloud.shredderAnim.gen()
                aniBlockGroupObj = blockGroup.newObj(this@Cloud, this)
            }
        }

        override fun onRemoved() {
            cloudRoom.offline(this)
        }

        override fun acceptItem(source: Building, item: Item) = false
        override fun sendData(receiver: IDataReceiver, item: Item, amount: Int) {
            receiver.receiveData(this, item, amount)
            info.lastReceiveOrSendDataTime = 0f
        }

        override fun acceptData(sender: IDataSender, item: Item) = true
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

        override fun handleItem(source: Building, item: Item) {}
        override fun handleStack(item: Item, amount: Int, source: Teamc) {}
        override fun canAcceptAnyData(sender: IDataSender) = true
        override fun isOutputting() = true
        override fun connect(sender: IDataSender) {
            info.sendersPos.add(sender.building.pos())
        }

        override fun disconnect(sender: IDataSender) {
            info.sendersPos.remove(sender.building.pos())
        }

        override fun draw() {
            WhenCanAniStateLoad {
                aniBlockGroupObj.update()
            }
            super.draw()
            WhenCanGlobalAnimationPlay {
                aniBlockGroupObj.drawBuilding()
            }
        }

        override fun drawSelect() {
            G.init()
            G.drawSurroundingCircle(tile, R.C.Cloud)

            CyberU.drawSenders(this, info.sendersPos)
        }

        override fun connectedReceiver(): Int? {
            return null
        }

        override fun connectedSenders(): ObjectSet<Int> = info.sendersPos
        override fun connectedSender(): Int? = info.sendersPos.first()
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
    }
}