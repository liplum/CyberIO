package net.liplum.utils

import arc.Core
import arc.math.geom.Vec2
import mindustry.Vars
import mindustry.core.World
import mindustry.gen.Building
import mindustry.world.Tile

object Screen {
    @JvmStatic
    fun Int.mouseXToTileX(): Int =
        tileX(this.toFloat())
    @JvmStatic
    fun Int.mouseXToTileY(): Int =
        tileY(this.toFloat())
    @JvmStatic
    fun tileXOnMouse(): Int =
        Core.input.mouseX().mouseXToTileX()
    @JvmStatic
    fun tileYOnMouse(): Int =
        Core.input.mouseY().mouseXToTileY()
    @JvmStatic
    fun tileOnMouse(): Tile? =
        Vars.world.tile(tileXOnMouse(), tileYOnMouse())
    @JvmStatic
    fun tileX(cursorX: Float): Int {
        val vec = Core.input.mouseWorld(cursorX, 0f)
        val input = Vars.control.input
        if (input.selectedBlock()) {
            vec.sub(input.block.offset, input.block.offset)
        }
        return World.toTile(vec.x)
    }
    @JvmStatic
    fun tileY(cursorY: Float): Int {
        val vec = Core.input.mouseWorld(0f, cursorY)
        val input = Vars.control.input
        if (input.selectedBlock()) {
            vec.sub(input.block.offset, input.block.offset)
        }
        return World.toTile(vec.y)
    }
    @JvmStatic
    fun tileAt(cursorX: Float, cursorY: Float): Tile? {
        val vec = Core.input.mouseWorld(cursorX, cursorY)
        val input = Vars.control.input
        if (input.selectedBlock()) {
            vec.sub(input.block.offset, input.block.offset)
        }
        return Vars.world.tileWorld(vec.x, vec.y)
    }
    @JvmStatic
    fun toWorld(cursorX: Float, cursorY: Float): Vec2 =
        Core.input.mouseWorld(cursorX, cursorY)
    @JvmStatic
    fun toWorld(cursorX: Int, cursorY: Int): Vec2 =
        Core.input.mouseWorld(cursorX.toFloat(), cursorY.toFloat())
    @JvmStatic
    fun worldOnMouse(): Vec2 =
        Core.input.mouseWorld()
    @JvmStatic
    fun buildOnMouse(): Building? {
        val input = Vars.control.input
        val worldXY = Core.input.mouseWorld(input.mouseX, input.mouseY)
        return Vars.world.buildWorld(worldXY.x, worldXY.y)
    }
}