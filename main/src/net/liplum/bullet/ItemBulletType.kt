package net.liplum.bullet

import arc.graphics.g2d.Draw
import arc.util.Tmp
import mindustry.Vars
import mindustry.content.Items
import mindustry.entities.bullet.BulletType
import mindustry.gen.Bullet
import mindustry.type.Item
import plumy.animation.ContextDraw.DrawScale

open class ItemBulletType : BulletType() {
    var drawSizer: Bullet.() -> Float = { 1f }
    fun Bullet.item(): Item {
        val itemID = fdata.toInt()
        val all = Vars.content.items()
        val item = if (itemID !in 0 until all.size) Items.copper else all[itemID]
        return item
    }

    override fun draw(b: Bullet) = b.run {
        super.draw(b)
        // Using fdata instead of data is for Prism
        val item = item()
        Draw.color(hitColor)
        item.fullIcon.DrawScale(x, y, drawSizer(), rotation())
    }

    override fun drawTrail(b: Bullet) = b.run {
        if (trailLength > 0 && trail != null) {
            val z = Draw.z()
            Draw.z(z - 0.0001f)
            val item = item()
            trail.draw(Tmp.c1.set(item.color).lerp(trailColor, 0.5f).a(item.color.a), trailWidth)
            Draw.z(z)
        }
    }
}