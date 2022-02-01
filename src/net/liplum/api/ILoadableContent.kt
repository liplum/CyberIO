package net.liplum.api

interface ILoadableContent {
    fun addLoadListener(listener: () -> Unit)
}