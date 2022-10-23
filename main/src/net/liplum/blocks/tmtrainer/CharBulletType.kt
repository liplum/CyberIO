package net.liplum.blocks.tmtrainer

import arc.graphics.Color
import mindustry.entities.Mover
import mindustry.entities.bullet.BulletType
import mindustry.game.Team
import mindustry.gen.Bullet
import mindustry.gen.Entityc
import mindustry.graphics.Layer
import plumy.dsl.DrawLayer
import plumy.core.ClientOnly
import net.liplum.render.Text

class CharBulletType : BulletType {
    constructor(speed: Float, damage: Float) : super(speed, damage)
    constructor() : super()

    override fun create(
        owner: Entityc?,
        team: Team?,
        x: Float,
        y: Float,
        angle: Float,
        damage: Float,
        velocityScl: Float,
        lifetimeScl: Float,
        data: Any?,
        mover: Mover?,
        aimX: Float,
        aimY: Float,
    ): Bullet? {
        return super.create(owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY)?.apply {
            ClientOnly {
                if (data == null) this.data = RandomName.randomColor()
                fdata = RandomName.randomCharIndex().toFloat()
            }
        }
    }

    override fun draw(b: Bullet) = b.run {
        super.draw(b)
        DrawLayer(Layer.bullet - 0.1f) {
            val char = RandomName.getChar(fdata.toInt())
            val color = data as? Color ?: Color.white
            Text.drawTextEasy(char, x, y, color, scale = 1f / hitSize * 2f)
        }
        return@run
    }
}