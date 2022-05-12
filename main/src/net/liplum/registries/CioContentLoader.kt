package net.liplum.registries

import net.liplum.gen.Contents

object CioContentLoader {
    @JvmStatic
    fun load() {
        Contents.load()
        CioTechTree.loadSerpulo()
    }
}