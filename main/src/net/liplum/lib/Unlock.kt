package net.liplum.lib

import arc.Core
import arc.Events
import mindustry.Vars
import mindustry.ctype.UnlockableContent
import net.liplum.UseReflection

@UseReflection
fun UnlockableContent.lock(): UnlockableContent {
    val isUnlocked = Core.settings.getBool("$name-unlocked", true)
    if (isUnlocked) {
        Core.settings.put("$name-unlocked", false)
        setFIn(UnlockableContent::class.java, "unlocked", false)
        Vars.state.rules.researched.remove(name)
        Events.fire(LockEvent(this))
        this.node()?.reset()
    }
    return this
}

class LockEvent(val content: UnlockableContent)