package net.liplum.api.brain

import mindustry.gen.Building
import net.liplum.api.ICyberEntity
import net.liplum.api.cyber.*
import net.liplum.common.delegates.Delegate
import net.liplum.common.utils.on
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.*

interface IHeimdallEntity : ICyberEntity {
    val scale: SpeedScale
    var brain: IBrain?
    val speedScale: Float
        get() = scale.value
    /**
     * It's belongs to [0,1]
     */
    var heatShared: Float
}

fun IHeimdallEntity.trigger(trigger: Trigger) {
    brain?.let { trigger.trigger(it.building) }
}

interface IBrain : IHeimdallEntity, Iterable<IUpgradeComponent> {
    /**
     * 4 sides of this brain block.
     * - 0 is [right]
     * - 1 is [top]
     * - 2 is [left]
     * - 3 is [bottom]
     */
    val sides: Sides
    override var brain: IBrain?
        get() = this
        set(_) {}
    val components: MutableSet<IUpgradeComponent>
    val onComponentChanged: Delegate
    val right: Side2
        get() = sides.right
    val top: Side2
        get() = sides.top
    val left: Side2
        get() = sides.left
    val bottom: Side2
        get() = sides.bottom
    val Direction2.sideObj: Side2
        get() = sides[this.side]
    var formationEffects: FormationEffects
    var shieldAmount: Float
    fun clear() {
        for (side in sides)
            side.clear()
        components.clear()
    }

    fun unlinkAll() {
        for (side in sides)
            side.unlinkAll()
    }
    /**
     * ## Contract
     * 1. This tile entity's size is 4
     * 2. The return value has 4 bits
     * ### For side (2 bits)
     * - 0 is [right]
     * - 1 is [top]
     * - 2 is [left]
     * - 3 is [bottom]
     * ### For part (2 bits)
     * - 0 is Up/Left
     * - 1 is Down/Right
     * @return the direction of [b] relative to this tile entity.
     */
    @Suppress("KotlinConstantConditions")
    fun sideOn(b: Building): Direction2 {
        val side = building.relativeTo(b).toInt()
        var res = side
        when (side) {
            //right
            0 -> {
                if (topRightX + 1 == b.topLeftX && topRightY == b.topLeftY)
                    res = res on Direction2.Part0Pos
                if (bottomRightX + 1 == b.bottomLeftX && bottomRightY == b.bottomLeftY)
                    res = res on Direction2.Part1Pos
            }
            // top
            1 -> {
                if (topLeftX == b.bottomLeftX && topLeftY + 1 == b.bottomLeftY)
                    res = res on Direction2.Part0Pos
                if (topRightX == b.bottomRightX && topRightY + 1 == b.bottomRightY)
                    res = res on Direction2.Part1Pos
            }
            // left
            2 -> {
                if (topLeftX - 1 == b.topRightX && topLeftY == b.topRightY)
                    res = res on Direction2.Part0Pos
                if (bottomLeftX - 1 == b.bottomRightX && bottomLeftY == b.bottomRightY)
                    res = res on Direction2.Part1Pos
            }
            // bottom
            3 -> {
                if (bottomLeftX == b.topLeftX && bottomLeftY - 1 == b.topLeftY)
                    res = res on Direction2.Part0Pos
                if (bottomRightX == b.topRightX && bottomRightY - 1 == b.topRightY)
                    res = res on Direction2.Part1Pos
            }
        }
        return Direction2(res)
    }
    /**
     * Doesn't guarantee the order.
     */
    override fun iterator() = components.iterator()

    companion object {
        fun Sides.getLeftSide(sideIndex: Int): Side2 =
            this[(sideIndex + 1) % 4]

        fun Sides.getRightSide(sideIndex: Int): Side2 =
            this[(sideIndex + 3) % 4]// is -1 actually, but prevent a negative index

        fun Sides.getOppositeSide(sideIndex: Int): Side2 =
            this[(sideIndex + 2) % 4]

        fun Sides.getLeftComponent(sideIndex: Int): IUpgradeComponent? =
            if (sideIndex == 0 || sideIndex == 3) // x.1
                getLeftSide(sideIndex)[1]
            else getLeftSide(sideIndex)[0]// x.0

        fun Sides.getRightComponent(sideIndex: Int): IUpgradeComponent? =
            if (sideIndex == 0 || sideIndex == 3) // x.1
                getRightSide(sideIndex)[1]
            else getRightSide(sideIndex)[0]// x.0

        inline fun IBrain.find(filter: (IUpgradeComponent) -> Boolean): IUpgradeComponent? {
            for (c in components)
                if (filter(c))
                    return c
            return null
        }
        @ClientOnly
        val XSign = arrayOf(+1, -1, -1, -1)
        @ClientOnly
        val YSign = arrayOf(+1, +1, +1, -1)
        @ClientOnly
        val Mirror = arrayOf(+1, +1, -1, -1)
    }
}
