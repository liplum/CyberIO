package net.liplum.function

import mindustry.Vars
import mindustry.gen.*

object MapCleaner {
    @JvmStatic
    fun cleanCurrentMap(modId: String, default: (Entityc) -> Unit = {}) {
        if (!Vars.state.isGame) return
        for (entity in Groups.all) {
            when (entity) {
                is Buildingc -> {
                    if (entity.block().minfo?.mod?.meta?.name == modId) {
                        entity.kill()
                    }
                }
                is Unitc -> {
                    if (entity.type().minfo?.mod?.meta?.name == modId) {
                        entity.kill()
                    }
                }
                else -> {
                    default(entity)
                }
            }
        }
    }
}
