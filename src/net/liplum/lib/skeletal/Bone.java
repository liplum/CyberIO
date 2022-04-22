package net.liplum.lib.skeletal;

import arc.math.geom.Vec2;
import arc.util.Tmp;
import org.jetbrains.annotations.NotNull;

public class Bone {
    public Skeleton sk;
    public Skin skin;
    /**
     * Always from a to b.
     * <ul>
     * <li>This is {@link Joint#nextBone} in ja</li>
     * <li>This is {@link Joint#preBone} in jb</li>
     * </ul>
     */
    public Joint ja, jb;
    /**
     * Mustn't be zero. It's 1.0 as default.
     */
    public float mass = 1f;
    /**
     * The length can't be zero.
     */
    public float length = 1f;

    public Bone(@NotNull Skeleton sk) {
        this.sk = sk;
    }

    public Bone() {
    }

    public float getRotation() {
        return jb.pos.angle(ja.pos);
    }

    public float getRealDst() {
        return jb.pos.dst(ja.pos);
    }

    /**
     * The positive means current bone is stretching,
     * while the negative means it's shrinking.
     *
     * @return the stretching or shrinking delta
     */
    public float getStretch() {
        return getRealDst() - length;
    }

    public void getDirectionA2B(Vec2 direction) {
        direction.set(jb.pos).minus(ja.pos).nor();
    }

    public void getDirectionB2A(Vec2 direction) {
        direction.set(ja.pos).minus(jb.pos).nor();
    }

    public void draw(float relativeX, float relativeY) {
        this.draw(relativeX, relativeY, 0f);
    }

    public void draw(float relativeX, float relativeY, float rotation) {
        Vec2 pos = Tmp.v1.set(jb.pos).minus(ja.pos).scl(0.5f);
        skin.draw(relativeX + pos.x + ja.pos.x, relativeY + pos.y + ja.pos.y, getRotation() + rotation);
    }

    public void setJa(Joint joint) {
        joint.sk = sk;
        ja = joint;
        ja.nextBone = this;
    }

    public void setJb(Joint joint) {
        joint.sk = sk;
        jb = joint;
        jb.preBone = this;
    }
}
