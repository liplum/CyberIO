package net.liplum.animations

interface ILoadableContent {
    fun addLoadListener(listener: () -> Unit)
}