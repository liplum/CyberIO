package net.liplum.lib.animations.ganim

interface IGlobalAnimation {
    val needUpdate: Boolean
    fun update()
}