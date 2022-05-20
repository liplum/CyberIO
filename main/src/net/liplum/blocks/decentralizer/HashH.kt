package net.liplum.blocks.decentralizer

import java.util.*

object HammingDistance {
    /**
     * Calculate the hamming distance between 2 bitsets which are of equal size.
     */
    @JvmStatic
    fun dst(a: BitSet, b: BitSet): Int {
        require(a.size() == b.size()) {
            "BitSets have different length: a:${a.size()}, b:${b.size()}"
        }
        var dist = 0
        for (i in 0 until a.size()) {
            if (a[i] != b[i]) dist++
        }
        return dist
    }
    /**
     * Calculate the hamming distance between 2 int(32 bits)
     */
    @JvmStatic
    fun dst(a: Int, b: Int): Int {
        var dist = 0
        var v = a xor b
        while (v != 0) {
            ++dist
            v = v and v - 1
        }
        return dist
    }
}
