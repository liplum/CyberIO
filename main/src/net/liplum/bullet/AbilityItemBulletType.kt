package net.liplum.bullet

import mindustry.gen.Bullet
import net.liplum.api.bullets.EmptyBulletAbility
import net.liplum.api.bullets.IBulletAbility

class AbilityItemBulletType : ItemBulletType() {
    var ability: IBulletAbility = EmptyBulletAbility
    override fun update(b: Bullet) {
        super.update(b)
        ability.update(b)
    }

    override fun draw(b: Bullet) {
        super.draw(b)
        ability.draw(b)
    }

    override fun init(b: Bullet) {
        super.init(b)
        ability.init(b)
    }

    override fun despawned(b: Bullet) {
        super.despawned(b)
        ability.despawned(b)
    }

    override fun init() {
        ability.preInit(this)
        super.init()
        ability.postInit(this)
    }
}