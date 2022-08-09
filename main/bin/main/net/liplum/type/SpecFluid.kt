package net.liplum.type

import arc.graphics.Color
import mindustry.type.Liquid
import net.liplum.util.atlasX

class SpecFluid : Liquid {
    constructor(name: String, color: Color) : super(name, color)
    constructor(name: String) : super(name)

    override fun loadIcon() {
        uiIcon = this.atlasX()
        fullIcon = this.atlasX()
    }
}