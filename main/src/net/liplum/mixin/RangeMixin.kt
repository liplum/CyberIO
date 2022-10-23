package net.liplum.mixin

import mindustry.logic.Ranged

class RangeMixin : TeamMixin(), Ranged {
    @JvmField
    var range = 0f
    override fun range(): Float = range
}