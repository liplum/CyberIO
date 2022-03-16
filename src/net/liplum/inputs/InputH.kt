package net.liplum.inputs

import arc.Core
import arc.util.Reflect
import mindustry.Vars
import mindustry.input.InputHandler
import mindustry.world.Tile

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

fun tileXOnMouse(): Int =
    Core.input.mouseX().mouseXToTileX()

fun tileYOnMouse(): Int =
    Core.input.mouseY().mouseXToTileY()

fun tileOnMouse(): Tile? =
    Vars.world.tile(tileXOnMouse(), tileYOnMouse())