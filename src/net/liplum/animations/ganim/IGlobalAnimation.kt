package net.liplum.animations.ganim

interface IGlobalAnimation {
    val needUpdate: Boolean
    fun update()
}