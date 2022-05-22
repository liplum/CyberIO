package net.liplum.blocks.ddos

import arc.graphics.g2d.Draw
import mindustry.Vars
import mindustry.content.Items
import mindustry.entities.bullet.BulletType
import mindustry.gen.Bullet
import mindustry.type.Item
import net.liplum.mdt.Draw

class ItemBulletType : BulletType() {
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
        item.fullIcon.Draw(x, y, rotation())
    }

    override fun drawTrail(b: Bullet) = b.run {
        if (trailLength > 0 && trail != null) {
            val z = Draw.z()
            Draw.z(z - 0.0001f)
            val item = item()
            trail.draw(item.color, trailWidth)
            Draw.z(z)
        }
    }
}