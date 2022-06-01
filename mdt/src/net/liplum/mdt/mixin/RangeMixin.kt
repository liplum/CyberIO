package net.liplum.mdt.mixin

import mindustry.logic.Ranged

class RangeMixin : TeamMixin(), Ranged {
    @JvmField
    var range = 0f
    override fun range(): Float = range
}