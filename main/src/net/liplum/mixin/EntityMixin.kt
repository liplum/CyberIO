package net.liplum.mixin

import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.entities.EntityGroup
import mindustry.gen.Entityc
import mindustry.gen.Groups
import mindustry.gen.Unitc

@Suppress("UNCHECKED_CAST")
open class EntityMixin : Entityc {
    @Transient @JvmField
    protected var added: Boolean = false
    @Transient @JvmField
    var id: Int = EntityGroup.nextId()
    override fun <T : Entityc> self(): T {
        return this as T
    }

    override fun <T : Any> `as`(): T {
        return this as T
    }

    override fun isAdded() = added
    override fun isLocal() =
        this == Vars.player || (this is Unitc && controller() == Vars.player)

    override fun isNull() = false
    override fun isRemote() =
        this is Unitc && isPlayer && !isLocal()

    override fun serialize() = false
    override fun classId(): Int {
        throw NotImplementedError("Should be impl by subclass")
    }

    override fun id() = id
    override fun id(id: Int) {
        this.id = id
    }

    override fun add() {
        if (!this.added) {
            Groups.all.add(this)
            this.added = true
        }
    }

    override fun afterRead() {
    }

    override fun read(read: Reads) {
        this.afterRead()
    }

    override fun remove() {
        if (added) {
            Groups.all.remove(this)
            added = false
        }
    }

    override fun update() {
    }

    override fun write(writes: Writes) {
    }
}