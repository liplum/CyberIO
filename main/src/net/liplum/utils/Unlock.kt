package net.liplum.utils

import arc.Core
import arc.Events
import mindustry.Vars
import mindustry.ctype.UnlockableContent
import mindustry.game.EventType.UnlockEvent
import net.liplum.common.UseReflection
import net.liplum.common.util.setFIn

@UseReflection
fun UnlockableContent.lock() {
    Core.settings.put("$name-unlocked", false)
    setFIn(UnlockableContent::class.java, "unlocked", false)
    Vars.state.rules.researched.remove(name)
    Events.fire(LockEvent(this))
    this.techNode?.reset()
}
@UseReflection
fun UnlockableContent.forceUnlock() {
    Core.settings.put("$name-unlocked", true)
    setFIn(UnlockableContent::class.java, "unlocked", true)
    Vars.state.rules.researched.add(name)
    Events.fire(UnlockEvent(this))
}

class LockEvent(val content: UnlockableContent)