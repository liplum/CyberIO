package net.liplum.lib;

import arc.graphics.g2d.TextureRegion;

public class Tx {
    public Tx(TextureRegion tr) {
        this.tr = tr;
    }

    /**
     * The offset of the true center for draw between {@linkplain Tx#tr texture} center.
     */
    public int dx, dy;
    /**
     * The offset of the 90-degree between {@linkplain Tx#tr texture} rotation.
     */
    public float dr;

    public TextureRegion tr;

    public float getX() {
        return dx;
    }

    public float getX(float relative) {
        return relative + dx;
    }

    public float getY() {
        return dy;
    }

    public float getY(float relative) {
        return relative + dy;
    }
    public float getRotation() {
        return dr;
    }

    public float getRotation(float relative) {
        return relative + dr;
    }
}
