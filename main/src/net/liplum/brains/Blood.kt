package net.liplum.brains

import arc.graphics.Color
import mindustry.type.Liquid
import mindustry.world.meta.Attribute
import net.liplum.R

/**
 * Copy from [Liquid]
 */
class Blood {
    var temperature = 0.5f
    var boilPoint = 2f
    var flammability = 0f
    var color: Color = R.C.Blood
    var gasColor: Color = Color.lightGray.cpy()
    /** @return true if blood will boil in this global environment.
     */
    fun willBoil(): Boolean {
        return Attribute.heat.env() >= boilPoint
    }
    fun canExtinguish(): Boolean {
        return flammability < 0.1f && temperature <= 0.5f
    }
    companion object{
        val X = Blood()
    }
}