@file:Suppress("UNUSED_VARIABLE", "LocalVariableName")

package net.liplum.test

import arc.math.Angles
import org.junit.jupiter.api.Test

class TestAngles {
    @Test
    fun `test angle`() {
        val range = 60f
        val `a 2 b` = Angles.angleDist(15f, 0f)
        val `b 2 a` = Angles.angleDist(0f, 15f)
        val `is within` = Angles.within(15f, 0f, 10f)
        println(`is within`)
    }
}