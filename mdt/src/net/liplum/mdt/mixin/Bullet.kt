package net.liplum.mdt.mixin

import mindustry.entities.Mover
import mindustry.gen.Bullet

inline fun Mover(crossinline move: Bullet.() -> Unit): Mover {
    return Mover {
        it.move()
    }
}