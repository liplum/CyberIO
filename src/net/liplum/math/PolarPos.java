package net.liplum.math;

import arc.math.Mathf;

public class PolarPos {
    public float r = 0;
    public float a = 0;

    public PolarPos(float r, float a) {
        this.r = r;
        this.a = a;
    }

    public PolarPos fromXY(float x, float y) {
        r = Mathf.sqrt(x * x + y * y);
        a = (float) Math.atan2(y, x);
        return this;
    }

    public static float toR(float x, float y) {
        return Mathf.sqrt(x * x + y * y);
    }

    public static float toA(float x, float y) {
        return (float) Math.atan2(y, x);
    }

    public PolarPos() {
    }

    public float toX() {
        return r * Mathf.cos(a);
    }

    public float toY() {
        return r * Mathf.sin(a);
    }

    public static PolarPos byXY(float x, float y) {
        return new PolarPos().fromXY(x, y);
    }
}
