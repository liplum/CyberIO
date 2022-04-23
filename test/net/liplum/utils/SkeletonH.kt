package net.liplum.utils

import net.liplum.lib.skeletal.Bone
import net.liplum.lib.skeletal.Skeleton

/**
 * Check whether it's circular or of bone-alone in this skeleton.
 */
fun Skeleton.check() {
    // First check bone-alone
    this.checkBoneAlone()
}
/**
 * Check whether a certain bone doesn't have its pre bone.
 */
fun Skeleton.checkBoneAlone() {
    fun check(pre: Bone?, cur: Bone) {
        if (pre != null) {
            if (pre !in cur.pre) {
                throw RuntimeException("Bone-alone exists in $this at $cur")
            }
        }
        for (nextBone in cur.next) {
            check(cur, nextBone)
        }
    }
    check(null, root)
}