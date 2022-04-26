package net.liplum.lib.skeletal

import arc.math.geom.Vec2

@MustBeDocumented
annotation class Changed
@MustBeDocumented
annotation class Invariant
@MustBeDocumented
annotation class UseVec(vararg val value: String)

fun Bone.getRelativePos(): Vec2 =
    sk.getRelativePos(this)

fun Bone.getAbsPos(@Invariant relative: Vec2): Vec2 =
    sk.getRelativePos(this).add(relative)