package net.liplum.brains

import arc.graphics.g2d.Draw
import arc.util.Time
import arc.util.Tmp
import mindustry.Vars
import mindustry.content.UnitTypes
import mindustry.gen.BlockUnitc
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import net.liplum.ClientOnly
import net.liplum.api.brain.*
import net.liplum.lib.Tx
import net.liplum.lib.skeletal.Bone
import net.liplum.lib.skeletal.Skeleton
import net.liplum.lib.skeletal.Skin
import net.liplum.lib.skeletal.getAbsPos
import net.liplum.utils.MdtUnit
import net.liplum.utils.inMod
import net.liplum.utils.radian

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
            scale = 0.5f
        }
    }

    open inner class HandBuild : Building(),
        IUpgradeComponent, ControlBlock {
        override var directionInfo: Direction2 = Direction2.Empty
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade> = this@Hand.upgrades
        var unit = UnitTypes.block.create(team) as BlockUnitc
        val upperArm: Bone
        val forearm: Bone
        val hand: Bone
        val skeleton = Skeleton().apply {
            isLinear = false
            val handSkin = Skin(HandTx).apply {
                dx = -1.5f
                dy = -0.5f
            }
            val boneSkin = Skin(BoneTx)
            val sk = this
            root = Bone(sk).apply {
                name = "UpperArm"
                skin = boneSkin
                upperArm = this
                length = 16f
                addNext(Bone(sk).apply {
                    name = "Forearm"
                    skin = boneSkin
                    forearm = this
                    length = 16f
                    addNext(Bone(sk).apply {
                        name = "Hand"
                        skin = handSkin
                        hand = this
                        length = 16f
                        mass = 5f
                        relative = true
                        limitAngle = true
                        minAngle = (-30f).radian
                        maxAngle = 30f.radian
                    })
                })
            }
        }

        override fun updateTile() {
            val p = Vars.player.unit()
            val px = p.x
            val py = p.y
            /*
            val angle = Angles.angleRad(forearmPos.x, forearmPos.y, px, py)
            val blockPlayerAngle = Angles.angleRad(px, py, x, y)
            val direction = MathU.towardRad(angle, blockPlayerAngle)
            val delta = MathU.angleDistRad(angle, blockPlayerAngle)
            val F = delta * 0.0005f
            forearm.applyAngularForce(F * direction)*/
            val forearmPos = forearm.getAbsPos(Tmp.v1.set(x, y))
            val F = Tmp.v1.set(px, py).minus(forearmPos).limit(0.008f)
            if (F.len() >= 0.0001f) {
                forearm.applyForce(F)
            }
            val handPos = hand.getAbsPos(Tmp.v1.set(x, y))
            val handF = Tmp.v1.set(px, py).minus(handPos).limit(0.08f)
            if (F.len() >= 0.0001f) {
                hand.applyForce(handF)
            }
            if (isControlled && unit.isShooting) {
                hand.applyForce(Tmp.v1.set(0f, 0.05f))
            }
            skeleton.update(Time.delta)
        }

        override fun draw() {
            super.draw()
            val forearmPos = forearm.getAbsPos(Tmp.v1.set(x, y))
            Draw.z(Layer.blockOver)
            Drawf.circles(forearmPos.x, forearmPos.y, 1f)
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