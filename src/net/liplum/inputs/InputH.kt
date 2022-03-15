package net.liplum.inputs

import arc.util.Reflect
import mindustry.Vars
import mindustry.input.InputHandler

fun Int.mouseXToTileX(): Int =
    Reflect.invoke(
        InputHandler::class.java,
        Vars.control.input, "tileX",
        arrayOf(this),
        Float::class.java
    )

fun Int.mouseXToTileY(): Int = Reflect.invoke(
    InputHandler::class.java,
    Vars.control.input, "tileY",
    arrayOf(this),
    Float::class.java
)