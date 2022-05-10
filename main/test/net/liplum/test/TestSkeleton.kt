package net.liplum.test

import net.liplum.lib.skeletal.Bone
import net.liplum.lib.skeletal.Skeleton
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("fast")
class TestSkeleton {
    val skeleton = Skeleton().apply {
        isLinear = false
        val sk = this
        root = Bone(sk).apply {
            name = "UpperArm"
            length = 16f
            id = curID++
            addNext(Bone(sk).apply {
                name = "Forearm"
                length = 16f
                id = curID++
                addNext(Bone(sk).apply {
                    name = "Hand"
                    length = 16f
                    mass = 5f
                    id = curID++
                })
            })
        }
    }
    @Test
    fun `Test find a bone by its name`() {
        assert(skeleton.findFirstByName("Forearm") != null) {
            "Can't find Forearm"
        }
    }
}