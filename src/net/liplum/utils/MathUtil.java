package net.liplum.utils;

import arc.math.Mathf;

public class MathUtil {
    public static float randomNP(float abs) {
        return Mathf.random(-abs, abs);
    }
}
