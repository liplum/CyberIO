package net.liplum.api.brain

import mindustry.gen.Building
import net.liplum.api.ICyberEntity
import net.liplum.api.cyber.*
import net.liplum.common.delegate.Delegate
import net.liplum.common.util.on
import net.liplum.utils.*

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
     *
     * ## Side.Pos
     * ```
     * ┌───┬───┬───┬───┐
     * │   │1.1│1.0│   │
     * ├───┼───┼───┼───┤
     * │2.0│ x │ x │0.1│
     * ├───┼───┼───┼───┤
     * │2.1│ x │ x │0.0│
     * ├───┼───┼───┼───┤
     * │   │3.0│3.1│   │
     * └───┴───┴───┴───┘
     * ```
     * @return the direction of [b] relative to this tile entity.
     */
    @Suppress("KotlinConstantConditions")
    fun sideOn(b: Building): Direction2 {
        val side = building.relativeTo(b).toInt()
        var res = side
        when (side) {
            //right
            0 -> {
                // top 0.1
                if (topRightX + 1 == b.topLeftX && topRightY == b.topLeftY)
                    res = res on Direction2.Part1Pos
                // bottom 0.0
                if (bottomRightX + 1 == b.bottomLeftX && bottomRightY == b.bottomLeftY)
                    res = res on Direction2.Part0Pos
            }
            // top
            1 -> {
                // left 1.1
                if (topLeftX == b.bottomLeftX && topLeftY + 1 == b.bottomLeftY)
                    res = res on Direction2.Part1Pos
                // right 1.0
                if (topRightX == b.bottomRightX && topRightY + 1 == b.bottomRightY)
                    res = res on Direction2.Part0Pos
            }
            // left
            2 -> {
                // top 2.0
                if (topLeftX - 1 == b.topRightX && topLeftY == b.topRightY)
                    res = res on Direction2.Part0Pos
                // bottom 2.1
                if (bottomLeftX - 1 == b.bottomRightX && bottomLeftY == b.bottomRightY)
                    res = res on Direction2.Part1Pos
            }
            // bottom
            3 -> {
                // left 3.0
                if (bottomLeftX == b.topLeftX && bottomLeftY - 1 == b.topLeftY)
                    res = res on Direction2.Part0Pos
                // right 3.1
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
        fun Sides.getLeftSide(side: Side): Side2 =
            this[(side + 1) % 4]

        fun Sides.getRightSide(side: Side): Side2 =
            this[(side + 3) % 4]// is -1 actually, but prevent a negative index

        fun Sides.getOppositeSide(side: Side): Side2 =
            this[(side + 2) % 4]

        inline fun IBrain.find(filter: (IUpgradeComponent) -> Boolean): IUpgradeComponent? {
            for (c in components)
                if (filter(c))
                    return c
            return null
        }
    }
}
