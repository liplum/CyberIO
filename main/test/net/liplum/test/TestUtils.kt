package net.liplum.test

import net.liplum.common.util.rotateOnce
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("fast")
class TestUtils {
    @Test
    fun `test rotate array`() {
        run {
            // Clockwise
            val a = arrayOf(1, 2, 3, 4, 5)
            val expected = arrayOf(5, 1, 2, 3, 4)
            a.rotateOnce(forward = true)
            assert(a.contentEquals(expected))
        }
        run {
            // Anticlockwise
            val a = arrayOf(1, 2, 3, 4, 5)
            val expected = arrayOf(2, 3, 4, 5, 1)
            a.rotateOnce(forward = false)
            assert(a.contentEquals(expected))
        }
    }
}