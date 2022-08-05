package net.liplum.common.util

import arc.Core
import arc.graphics.Pixmap
import arc.graphics.Texture
import arc.graphics.Texture.TextureFilter
import plumy.core.assets.TR
import java.io.InputStream

fun loadDynamic(input: InputStream): TR {
    val pixmapBytes = input.readBytes()
    val pix = Pixmap(pixmapBytes)
    val texture = Texture(pix)
    texture.setFilter(TextureFilter.linear)
    return TR(texture)
}

fun loadDynamicAsync(input: InputStream, callback: (TR) -> Unit) {
    val pixmapBytes = input.readBytes()
    val pix = Pixmap(pixmapBytes)
    Core.app.post {
        val texture = Texture(pix)
        texture.setFilter(TextureFilter.linear)
        val tr = TR(texture)
        callback(tr)
    }
}