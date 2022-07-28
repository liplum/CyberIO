package net.liplum.heimdall

import mindustry.ctype.UnlockableContent

interface IResourceGeneratingContext {
    fun genID(): ResourceID
    fun randReserve(): ResourceReserve
    fun randTimeReq(meta: ResourceMeta, reserve: ResourceReserve): Int
}

interface IResourceMetaMapping {
    operator fun get(context: UnlockableContent): ResourceMeta?
}