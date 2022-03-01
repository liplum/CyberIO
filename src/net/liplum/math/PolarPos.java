package net.liplum.math;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;

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

    public PolarPos fromV2d(Vec2 v2d) {
        fromXY(v2d.x, v2d.y);
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

    public static PolarPos byV2d(Vec2 v2d) {
        return new PolarPos().fromV2d(v2d);
    }

    public Vec2 toV2d() {
        return new Vec2(toX(), toY());
    }

    public Vec3 toV3d() {
        return new Vec3(toX(), toY(), 1);
    }
}
