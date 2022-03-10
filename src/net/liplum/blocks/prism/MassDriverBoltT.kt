package net.liplum.blocks.prism

import arc.graphics.Color
import arc.graphics.g2d.Draw
import mindustry.entities.bullet.MassDriverBolt
import mindustry.gen.Bullet

open class MassDriverBoltT : MassDriverBolt() {
    lateinit var tintColor: Color
    lateinit var tintBKColor: Color
    override fun draw(b: Bullet) {
        val w = 11f
        val h = 13f
        Draw.color(tintBKColor)
        Draw.rect("shell-back", b.x, b.y, w, h, b.rotation() + 90)
        Draw.color(tintColor)
        Draw.rect("shell", b.x, b.y, w, h, b.rotation() + 90)
        Draw.reset()
    }
}