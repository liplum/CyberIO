package net.liplum.lib.math;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;

/**
 * It represents a polar coordinate using radian.
 */
public class Polar {
    public float r = 0;
    public float a = 0;

    public Polar(float r, float a) {
        this.r = r;
        this.a = a;
    }

    public Polar() {
    }

    public static float toR(float x, float y) {
        return Mathf.sqrt(x * x + y * y);
    }

    public static float toA(float x, float y) {
        return (float) Math.atan2(y, x);
    }

    public static Polar byXY(float x, float y) {
        return new Polar().fromXY(x, y);
    }

    public static Polar byV2d(Vec2 v2d) {
        return new Polar().fromV2d(v2d);
    }

    public Polar fromXY(float x, float y) {
        r = Mathf.sqrt(x * x + y * y);
        a = (float) Math.atan2(y, x);
        return this;
    }

    public Polar fromV2d(Vec2 v2d) {
        fromXY(v2d.x, v2d.y);
        return this;
    }

    public float getX() {
        return r * Mathf.cos(a);
    }

    public float getY() {
        return r * Mathf.sin(a);
    }

    public Vec2 getV2d() {
        return new Vec2(getX(), getY());
    }

    public Vec3 getV3d() {
        return new Vec3(getX(), getY(), 1);
    }
}
