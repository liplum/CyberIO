package net.liplum.utils;

import arc.math.Mathf;

public class Util2D {
    public static float distance(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return Mathf.sqrt(x * x + y * y);
    }
}
