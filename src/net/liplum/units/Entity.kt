package net.liplum.units

import arc.math.geom.Position
import arc.math.geom.Vec2
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Drawc
import mindustry.gen.Entityc
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.environment.Floor

open class Entity : Drawc, Position by Vec2() {
    override fun isAdded(): Boolean {
        TODO("Not yet implemented")
    }
    override fun update() {
        TODO("Not yet implemented")
    }
    override fun remove() {
        TODO("Not yet implemented")
    }
    override fun add() {
        TODO("Not yet implemented")
    }
    override fun isLocal(): Boolean {
        TODO("Not yet implemented")
    }
    override fun isRemote(): Boolean {
        TODO("Not yet implemented")
    }
    override fun isNull(): Boolean {
        TODO("Not yet implemented")
    }
    override fun <T : Entityc?> self(): T {
        TODO("Not yet implemented")
    }
    override fun <T : Any?> `as`(): T {
        TODO("Not yet implemented")
    }
    override fun classId(): Int {
        TODO("Not yet implemented")
    }
    override fun serialize(): Boolean {
        TODO("Not yet implemented")
    }
    override fun read(p0: Reads?) {
        TODO("Not yet implemented")
    }
    override fun write(p0: Writes?) {
        TODO("Not yet implemented")
    }
    override fun afterRead() {
        TODO("Not yet implemented")
    }
    override fun id(): Int {
        TODO("Not yet implemented")
    }
    override fun id(p0: Int) {
        TODO("Not yet implemented")
    }
    override fun set(p0: Float, p1: Float) {
        TODO("Not yet implemented")
    }
    override fun set(p0: Position?) {
        TODO("Not yet implemented")
    }
    override fun trns(p0: Float, p1: Float) {
        TODO("Not yet implemented")
    }
    override fun trns(p0: Position?) {
        TODO("Not yet implemented")
    }
    override fun tileX(): Int {
        TODO("Not yet implemented")
    }
    override fun tileY(): Int {
        TODO("Not yet implemented")
    }
    override fun floorOn(): Floor {
        TODO("Not yet implemented")
    }
    override fun blockOn(): Block {
        TODO("Not yet implemented")
    }
    override fun onSolid(): Boolean {
        TODO("Not yet implemented")
    }
    override fun tileOn(): Tile {
        TODO("Not yet implemented")
    }
    override fun x(): Float {
        TODO("Not yet implemented")
    }
    override fun x(p0: Float) {
        TODO("Not yet implemented")
    }
    override fun y(): Float {
        TODO("Not yet implemented")
    }
    override fun y(p0: Float) {
        TODO("Not yet implemented")
    }
    override fun clipSize(): Float {
        TODO("Not yet implemented")
    }
    override fun draw() {
        TODO("Not yet implemented")
    }
}