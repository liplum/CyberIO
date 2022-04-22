package net.liplum.brains

import arc.util.Time
import arc.util.Tmp
import mindustry.content.UnitTypes
import mindustry.gen.BlockUnitc
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import net.liplum.ClientOnly
import net.liplum.api.brain.*
import net.liplum.lib.Tx
import net.liplum.lib.skeletal.Bone
import net.liplum.lib.skeletal.Skeleton
import net.liplum.lib.skeletal.Skin
import net.liplum.utils.MdtUnit
import net.liplum.utils.inMod

open class Hand(name: String) : Block(name), IComponentBlock {
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()
    @ClientOnly lateinit var BoneTx: Tx
    @ClientOnly lateinit var JointTx: Tx
    var reloadTime = 60f

    init {
        update = true
        solid = true
    }

    override fun load() {
        super.load()
        BoneTx = Tx(this.inMod("bone"))
        JointTx = Tx(this.inMod("joint"))
    }

    open inner class HandBuild : Building(),
        IUpgradeComponent, ControlBlock {
        override var directionInfo: Direction2 = Direction2.Empty
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade> = this@Hand.upgrades
        var unit = UnitTypes.block.create(team) as BlockUnitc
        val bone2: Bone
        val bone3: Bone
        val skeleton = Skeleton().apply {
            val jointSkin = Skin(JointTx)
            val boneSkin = Skin(BoneTx).apply {
                texture.dr = -90f
            }
            val sk = this
            root = Bone(sk).apply {
                // bone 1
                skin = boneSkin
                length = 16f
                setNext(Bone(sk).apply {
                    // bone 2
                    skin = boneSkin
                    length = 16f
                    bone2 = this
                    setNext(Bone(sk).apply {
                        // bone 3
                        bone3 = this
                        skin = boneSkin
                        length = 16f
                    })
                })
            }
        }

        override fun updateTile() {
            bone2.applyForce(Tmp.v1.set(0f, 0.01f))
            if (isControlled && unit.isShooting) {
                bone3.applyForce(Tmp.v1.set(0f, 0.04f))
            }
            skeleton.update(Time.delta)
        }

        override fun draw() {
            super.draw()
            skeleton.drawLinear(x, y, 0f)
        }

        override fun unit(): MdtUnit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as MdtUnit)
        }
    }
}