package net.liplum.type

import arc.graphics.Color
import mindustry.type.Item
import net.liplum.utils.atlasX

class SpecItem : Item {
    constructor(name: String, color: Color) : super(name, color)
    constructor(name: String) : super(name)

    override fun loadIcon() {
        uiIcon = this.atlasX()
        fullIcon = this.atlasX()
    }
}