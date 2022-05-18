package net.liplum.brains

import arc.util.Time
import arc.util.Tmp
import mindustry.content.UnitTypes
import mindustry.gen.BlockUnitc
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import net.liplum.mdt.ClientOnly
import net.liplum.api.brain.*
import net.liplum.lib.Tx
import net.liplum.lib.skeletal.Bone
import net.liplum.lib.skeletal.Skeleton
import net.liplum.lib.skeletal.Skin
import net.liplum.mdt.utils.MdtUnit
import net.liplum.mdt.utils.inMod

open class Hand(name: String) : Block(name), IComponentBlock {
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()
    @ClientOnly lateinit var BoneTx: Tx
    @ClientOnly lateinit var HandTx: Tx
    var reloadTime = 60f

    init {
        update = true
        solid = true
    }

    override fun load() {
        super.load()
        BoneTx = Tx(this.inMod("bone")).apply {
            dr = -90f
            scale = 0.5f
        }
        HandTx = Tx(this.inMod("hand")).apply {
            dr = 90f
            dx = -3f
            dy = -1f
            scale = 0.5f
        }
    }

    open inner class HandBuild : Building(),
        IUpgradeComponent, ControlBlock {
        //<editor-fold desc="Heimdall">
        override val scale: SpeedScale = SpeedScale()
        override var directionInfo: Direction2 = Direction2.Empty
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade> = this@Hand.upgrades
        override var heatShared = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        //</editor-fold>
        var unit = UnitTypes.block.create(team) as BlockUnitc
        val forearm: Bone
        val skeleton = Skeleton().apply {
            isLinear = false
            val handSkin = Skin(HandTx)
            val boneSkin = Skin(BoneTx)
            val sk = this
            root = Bone(sk).apply {
                name = "UpperArm"
                skin = boneSkin
                length = 16f
                id = curID++
                addNext(Bone(sk).apply {
                    name = "Forearm"
                    skin = boneSkin
                    length = 16f
                    forearm = this
                    id = curID++
                    addNext(Bone(sk).apply {
                        name = "Hand"
                        skin = handSkin
                        length = 16f
                        mass = 5f
                        id = curID++
                    })
                })
            }
        }
        override fun delta(): Float {
            return this.timeScale * Time.delta * speedScale
        }
        override fun updateTile() {
            scale.update()
            skeleton.findFirstByName("UpperArm")?.apply {
                applyForce(Tmp.v1.set(0f, -0.001f))
            }
            forearm.applyForce(Tmp.v1.set(0f, 0.001f))
            if (isControlled && unit.isShooting) {
                forearm.next.forEach {
                    it.applyForce(Tmp.v1.set(0f, 0.005f))
                }
            }
            skeleton.update(Time.delta)
        }

        override fun draw() {
            super.draw()
            skeleton.findFirstByName("Hand")?.skin?.texture?.apply {
                dx = -1.5f
                dy = -0.5f
            }
            skeleton.draw(x, y, 0f)
        }

        override fun unit(): MdtUnit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as MdtUnit)
        }
    }
}