package net.liplum.mixin

import arc.math.geom.Position
import arc.math.geom.Vec2
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.core.World
import mindustry.gen.Building
import mindustry.gen.Posc
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.environment.Floor

open class PosMixin(
    val pos: Vec2 = Vec2(),
) : EntityMixin(), Posc, Position by pos {
    override fun floorOn(): Floor {
        val tile = tileOn()
        return if (tile != null && tile.block() == Blocks.air) tile.floor()
        else Blocks.air as Floor
    }

    override fun buildOn(): Building? =
        Vars.world.buildWorld(pos.x, pos.y)

    override fun onSolid(): Boolean =
        tileOn()?.solid() ?: true

    override fun x(): Float = pos.x
    override fun x(newX: Float) {
        pos.x = newX
    }

    override fun y(): Float = pos.y
    override fun y(newY: Float) {
        pos.y = newY
    }

    override fun tileX(): Int = World.toTile(pos.x)
    override fun tileY(): Int = World.toTile(pos.y)
    override fun blockOn(): Block? {
        val tile = tileOn()
        return tile?.block() ?: Blocks.air
    }

    override fun tileOn(): Tile? =
        Vars.world.tileWorld(x, y)

    override fun set(p: Position) =
        set(p.x, p.y)

    override fun set(x: Float, y: Float) {
        pos.x = x
        pos.y = y
    }

    override fun trns(p: Position) {
        pos.trns(p.x, p.y)
    }

    override fun trns(x: Float, y: Float) {
        pos.trns(x, y)
    }
}